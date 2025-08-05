package bett.gustavo.rinhaBackend2025Api.consumer;

import bett.gustavo.rinhaBackend2025Api.config.ApiServiceConfig;
import bett.gustavo.rinhaBackend2025Api.dto.PaymentDtoSender;
import bett.gustavo.rinhaBackend2025Api.service.ApiService;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.service.Common;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class PaymentConsumer {

    @Autowired
    private ApiServiceConfig apiServiceConfig;

    @Autowired
    private WebClient.Builder builder;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    @Qualifier("reactiveRedisTemplatePayment")
    private ReactiveRedisTemplate<String, Payment> reactiveRedisTemplate;

    private static final int MAX_CONCURRENCY = 10;
    private static final int MAX_RETRIES = 5;

    @PostConstruct
    public void init() {
        subscribeToChannel(Common.PAYMENT_QUEUE);
    }

    private void subscribeToChannel(String channelName) {
        reactiveRedisTemplate.listenToChannel(channelName)
                .flatMap(msg -> processPayment(msg.getMessage())
                                .retryWhen(
                                        Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                                                .maxBackoff(Duration.ofSeconds(10))
                                                .jitter(0.5)
                                ), MAX_CONCURRENCY
                        , MAX_CONCURRENCY)
                .subscribe();
    }

    private Mono<Void> processPayment(Payment payment) {
        PaymentDtoSender dto = PaymentDtoSender.from(payment);

        return checkHealthAndSend(payment, dto, true)
                .onErrorResume(ex -> checkHealthAndSend(payment, dto, false))
                .switchIfEmpty(retryLater(payment))
                .then();
    }

    private Mono<Void> checkHealthAndSend(Payment payment, PaymentDtoSender dto, boolean isDefault) {
        ApiService api = isDefault ? apiServiceConfig.defaultApiService(builder) : apiServiceConfig.fallbackApiService(builder);

        return api.getHealthCheck()
                .timeout(Duration.ofSeconds(1))
                .filter(health -> health != null && !health.failing())
                .flatMap(health -> sendToApi(payment, dto, isDefault))
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(ex -> Mono.empty());
    }

    private Mono<Void> sendToApi(Payment payment, PaymentDtoSender dto, boolean isDefault) {
        ApiService api = isDefault ? apiServiceConfig.defaultApiService(builder) : apiServiceConfig.fallbackApiService(builder);

        return api.sendPayments(dto)
                .flatMap(success -> Mono.fromRunnable(() -> {
                    Payment updated = Payment.atualizaPayment(payment, dto.requestedAt(), dto.requestedAtSeconds(),
                            isDefault ? SituationPayment.DEFAULT : SituationPayment.FALLBACK);
                    if (isDefault) {
                        paymentService.saveDefault(updated);
                    } else {
                        paymentService.saveFallback(updated);
                    }
                }).then());
    }

    private Mono<Void> retryLater(Payment payment) {
        return Mono.delay(Duration.ofSeconds(5))
                .flatMap(t -> reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment))
                .then();
    }

}