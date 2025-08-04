package bett.gustavo.rinhaBackend2025Api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentsSummaryDto {

    private Long totalRequests;
    private BigDecimal totalAmount;
    private BigDecimal totalFee;
    private BigDecimal feePerTransaction;
}
