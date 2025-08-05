package bett.gustavo.rinhaBackend2025Api.dto;

public record HealthCheckDto(
        Boolean failing,
        Long minResponseTime
) {}