package bett.gustavo.rinhaBackend2025Consumer.service;

import bett.gustavo.rinhaBackend2025Consumer.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class ApiService {
    private final WebClient webClient;

    public ApiService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<ResponsePaymentApi> sendPayments(PaymentDtoSender paymentDtoSender) {
        return webClient
                .post()
                .uri("/payments")
                .bodyValue(paymentDtoSender)
                .retrieve()
                .bodyToMono(ResponsePaymentApi.class);
    }

    // 1 chamada a cada 5 segundos, caso passe -> 429 - too many requests
    public Mono<HealthCheckDto> getHealthCheck() {
        return webClient
                .get()
                .uri("/payments/service-health")
                .retrieve()
                .bodyToMono(HealthCheckDto.class);
    }

    public Mono<ResponsePaymentApi> getPayments(UUID uuid) {
        return webClient
                .get()
                .uri("/payments", uuid.toString())
                .retrieve()
                .bodyToMono(ResponsePaymentApi.class);
    }

    //Endpoints Administrativos de Payment Processor


    public Mono<PaymentsSummaryDto> getPaymentsSummary(ZonedDateTime from, ZonedDateTime to) {
        return webClient
                .get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/admin/payments-summary");
                    if (from != null) {
                        builder.queryParam("from", from.toString());
                    }
                    if (to != null) {
                        builder.queryParam("to", to.toString());
                    }
                    return builder.build();
                })
                .header("X-Rinha-Token", new TokenDto().getToken())
                .retrieve()
                .bodyToMono(PaymentsSummaryDto.class);
    }

    public Mono<String> setToken(TokenDto tokenDto) {
        return webClient
                .put()
                .uri("/admin/configurations/token")
                .bodyValue(tokenDto)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> setDelay(DelayDto delayDto) {
        return webClient
                .put()
                .uri("/admin/configurations/delay")
                .bodyValue(delayDto)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> setFailure(FailureDto failureDto) {
        return webClient
                .put()
                .uri("/admin/configurations/failure")
                .bodyValue(failureDto)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> purgeDatabase() {
        return webClient
                .post()
                .uri("/admin/purge-payments")
                .retrieve()
                .bodyToMono(String.class);
    }
}
