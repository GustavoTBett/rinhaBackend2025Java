package bett.gustavo.rinhaBackend2025Api.consumer;

import bett.gustavo.rinhaBackend2025Api.config.ApiServiceConfig;
import bett.gustavo.rinhaBackend2025Api.dto.PaymentDtoSender;
import bett.gustavo.rinhaBackend2025Api.service.ApiService;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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

    private final String queueName = "paymentQueue";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 1000)
    public void listesQueue() throws JsonProcessingException {
        String json = redisTemplate.opsForList().rightPop(queueName);
        Payment payment = new ObjectMapper().readValue(json, Payment.class);
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
                                        String json = null;
                                        try {
                                            json = new ObjectMapper().writeValueAsString(payment);
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        redisTemplate.opsForList().leftPush(queueName, json);
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
                    String json = null;
                    try {
                        json = new ObjectMapper().writeValueAsString(payment);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    redisTemplate.opsForList().leftPush(queueName, json);
                })
                .doOnSuccess(success -> {
                    payment.setSituation(situation);
                    paymentService.save(payment);
                })
                .subscribe();
    }

}
