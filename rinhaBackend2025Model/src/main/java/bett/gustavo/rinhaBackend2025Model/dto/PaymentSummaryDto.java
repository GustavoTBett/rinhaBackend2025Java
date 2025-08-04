package bett.gustavo.rinhaBackend2025Model.dto;

import lombok.Data;

import java.math.BigDecimal;

public record PaymentSummaryDto(
        int totalRequests,
        BigDecimal totalAmount
) {}
