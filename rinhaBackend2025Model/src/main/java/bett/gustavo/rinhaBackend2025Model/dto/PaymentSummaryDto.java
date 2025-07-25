package bett.gustavo.rinhaBackend2025Model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentSummaryDto {

    private int totalRequests;
    private BigDecimal totalAmount;

    public PaymentSummaryDto(int totalRequests, BigDecimal totalAmount) {
        this.totalRequests = totalRequests;
        this.totalAmount = totalAmount;
    }
}
