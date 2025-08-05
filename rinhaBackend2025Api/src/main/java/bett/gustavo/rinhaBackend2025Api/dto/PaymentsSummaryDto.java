package bett.gustavo.rinhaBackend2025Api.dto;

import java.math.BigDecimal;

public record PaymentsSummaryDto(
        Long totalRequests,
        BigDecimal totalAmount,
        BigDecimal totalFee,
        BigDecimal feePerTransaction
) {}
