package bett.gustavo.rinhaBackend2025Api.dto;

import lombok.Data;

@Data
public class HealthCheckDto {

    private Boolean failing;
    private Long minResponseTime;
}
