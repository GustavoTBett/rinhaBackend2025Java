package bett.gustavo.rinhaBackend2025Consumer.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentsSummaryDto {

    private Long totalRequests;
    private BigDecimal totalAmount;
    private BigDecimal totalFee;
    private BigDecimal feePerTransaction;
}
