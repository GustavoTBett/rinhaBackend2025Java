package bett.gustavo.rinhaBackend2025Api.dto;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class PaymentDtoSender {

    private String correlationId;
    private BigDecimal amount;
    private String requestedAt;
    private Long requestedAtSeconds;

    public PaymentDtoSender(Payment payment) {
        this.correlationId = payment.getCorrelationId().toString();
        this.amount = payment.getAmount();
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        this.requestedAtSeconds = nowUtc.toEpochSecond();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String formattedDate = nowUtc.format(formatter);
        this.requestedAt = formattedDate;
    }
}
