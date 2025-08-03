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

    @PostConstruct
    public void init() {
        subscribeToChannel(Common.PAYMENT_QUEUE);
    }

    private void subscribeToChannel(String channelName) {
        reactiveRedisTemplate.listenToChannel(channelName)
                .doOnNext(msg -> {
                    Payment payment = msg.getMessage();

                    PaymentDtoSender paymentDtoSender = new PaymentDtoSender(payment);
                    payment.setCreatedAt(paymentDtoSender.getRequestedAt());
                    payment.setCreateAtSeconds(paymentDtoSender.getRequestedAtSeconds());

                    checkAndSend(payment, paymentDtoSender);
                })
                .subscribe();
    }


    private void checkAndSend(Payment payment, PaymentDtoSender dto) {
        apiServiceConfig.defaultApiService(builder)
                .getHealthCheck()
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(ex -> Mono.empty())
                .subscribe(health -> {
                    if (health != null && !health.getFailing()) {
                        sendToApi(payment, dto, true);
                    } else {
                        apiServiceConfig.fallbackApiService(builder)
                                .getHealthCheck()
                                .timeout(Duration.ofSeconds(2))
                                .onErrorResume(ex -> Mono.empty())
                                .subscribe(fallbackHealth -> {
                                    if (fallbackHealth != null && !fallbackHealth.getFailing()) {
                                        sendToApi(payment, dto, false);
                                    } else {
                                        reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment);
                                    }
                                });
                    }
                });
    }

    private void sendToApi(Payment payment, PaymentDtoSender dto, boolean isDefault) {
        ApiService api = isDefault ? apiServiceConfig.defaultApiService(builder)
                : apiServiceConfig.fallbackApiService(builder);

        api.sendPayments(dto)
                .doOnError(error -> {
                    reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment);
                })
                .doOnSuccess(success -> {
                    if (isDefault) {
                        payment.setSituation(SituationPayment.DEFAULT);
                        paymentService.saveDefault(payment);
                    } else {
                        payment.setSituation(SituationPayment.FALLBACK);
                        paymentService.saveFallback(payment);
                    }
                })
                .subscribe();
    }

}
