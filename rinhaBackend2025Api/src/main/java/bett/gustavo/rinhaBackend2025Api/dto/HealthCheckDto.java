package bett.gustavo.rinhaBackend2025Api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HealthCheckDto {

    private Boolean failing;
    private Long minResponseTime;
}
