package bett.gustavo.rinhaBackend2025Api.config;

import bett.gustavo.rinhaBackend2025Api.service.ApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiServiceConfig {

    @Value("${payment.processor.url.default}")
    private String PAYMENT_PROCESSOR_URL_DEFAULT;

    @Value("${payment.processor.url.fallback}")
    private String PAYMENT_PROCESSOR_URL_FALLBACK;

    @Bean
    public ApiService defaultApiService(WebClient.Builder builder) {
        return new ApiService(builder.baseUrl(PAYMENT_PROCESSOR_URL_DEFAULT));
    }

    @Bean
    public ApiService fallbackApiService(WebClient.Builder builder) {
        return new ApiService(builder.baseUrl(PAYMENT_PROCESSOR_URL_FALLBACK));
    }
}
