package bett.gustavo.rinhaBackend2025Api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public record HealthCheckDto(
        Boolean failing,
        Long minResponseTime
) {}