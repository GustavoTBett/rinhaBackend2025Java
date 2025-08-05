package bett.gustavo.rinhaBackend2025Api.consumer;

import bett.gustavo.rinhaBackend2025Api.config.ApiServiceConfig;
import bett.gustavo.rinhaBackend2025Api.dto.PaymentDtoSender;
import bett.gustavo.rinhaBackend2025Api.service.ApiService;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.service.Common;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
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

    private final AtomicBoolean isDefaultHealthy = new AtomicBoolean(true);
    private final AtomicBoolean isFallbackHealthy = new AtomicBoolean(true);

    @PostConstruct
    public void init() {
        monitorHealth();
        subscribeToChannel(Common.PAYMENT_QUEUE);
    }

    private void monitorHealth() {
        Flux.interval(Duration.ofSeconds(1))
                .flatMap(i -> Mono.zip(
                        apiServiceConfig.defaultApiService(builder).getHealthCheck()
                                .timeout(Duration.ofMillis(300))
                                .onErrorResume(e -> Mono.empty())
                                .doOnNext(h -> isDefaultHealthy.set(!h.failing())),

                        apiServiceConfig.fallbackApiService(builder).getHealthCheck()
                                .timeout(Duration.ofMillis(300))
                                .onErrorResume(e -> Mono.empty())
                                .doOnNext(h -> isFallbackHealthy.set(!h.failing()))
                ))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void subscribeToChannel(String channelName) {
        reactiveRedisTemplate.listenToChannel(channelName)
                .map(message -> message.getMessage())
                .flatMap(this::handlePayment, 16)
                .onErrorContinue((err, obj) -> log.error("Erro no processamento do pagamento", err))
                .subscribe();
    }

    private Mono<Void> handlePayment(Payment payment) {
        PaymentDtoSender dto = PaymentDtoSender.from(payment);

        if (isDefaultHealthy.get()) {
            return sendToApi(payment, dto, true);
        } else if (isFallbackHealthy.get()) {
            return sendToApi(payment, dto, false);
        } else {
            return reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment).then();
        }
    }

    private Mono<Void> sendToApi(Payment payment, PaymentDtoSender dto, boolean isDefault) {
        ApiService api = isDefault
                ? apiServiceConfig.defaultApiService(builder)
                : apiServiceConfig.fallbackApiService(builder);

        return api.sendPayments(dto)
                .timeout(Duration.ofMillis(300))
                .doOnError(e -> log.error("Erro ao enviar pagamento: {}", e.getMessage()))
                .flatMap(success -> Mono.fromRunnable(() -> {
                    Payment updated = Payment.atualizaPayment(payment, dto.requestedAt(), dto.requestedAtSeconds(),
                            isDefault ? SituationPayment.DEFAULT : SituationPayment.FALLBACK);
                    if (isDefault) {
                        paymentService.saveDefault(updated);
                    } else {
                        paymentService.saveFallback(updated);
                    }
                }).then())
                .onErrorResume(e -> {
                    return reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment).then();
                });
    }
}

