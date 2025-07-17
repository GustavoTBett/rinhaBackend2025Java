package bett.gustavo.rinhaBackend2025Consumer.dto;

import lombok.Data;

@Data
public class HealthCheckDto {

    private Boolean failing;
    private Long minResponseTime;
}
