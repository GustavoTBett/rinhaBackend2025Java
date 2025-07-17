package bett.gustavo.rinhaBackend2025Consumer.config;

import bett.gustavo.rinhaBackend2025Consumer.service.ApiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiServiceConfig {

    @Bean
    public ApiService defaultApiService(WebClient.Builder builder) {
        return new ApiService(builder.baseUrl("http://localhost:8001"));
    }

    @Bean
    public ApiService fallbackApiService(WebClient.Builder builder) {
        return new ApiService(builder.baseUrl("http://localhost:8002"));
    }
}
