package bett.gustavo.rinhaBackend2025Consumer.consumer;

import bett.gustavo.rinhaBackend2025Consumer.config.ApiServiceConfig;
import bett.gustavo.rinhaBackend2025Consumer.dto.PaymentDtoSender;
import bett.gustavo.rinhaBackend2025Consumer.service.ApiService;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queuesToDeclare = @Queue("payments"))
    public void listesQueue(Payment payment) {
        PaymentDtoSender paymentDtoSender = new PaymentDtoSender(payment);
        payment.setCreatedAt(paymentDtoSender.getRequestedAt());
        payment.setCreateAtSeconds(paymentDtoSender.getRequestedAtSeconds());

        checkAndSend(payment, paymentDtoSender);
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
                                        rabbitTemplate.convertAndSend("payments", payment);
                                    }
                                });
                    }
                });
    }

    private void sendToApi(Payment payment, PaymentDtoSender dto, boolean isDefault) {
        ApiService api = isDefault ? apiServiceConfig.defaultApiService(builder)
                : apiServiceConfig.fallbackApiService(builder);

        SituationPayment situation = isDefault ? SituationPayment.DEFAULT
                : SituationPayment.FALLBACK;

        api.sendPayments(dto)
                .doOnError(error -> {
                    rabbitTemplate.convertAndSend("payments", payment);
                })
                .doOnSuccess(success -> {
                    payment.setSituation(situation);
                    if (isDefault) {
                        paymentService.saveDefault(payment);
                    } else {
                        paymentService.saveFallback(payment);
                    }
                })
                .subscribe();
    }

}
