package bett.gustavo.rinhaBackend2025Api.dto;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record PaymentDtoSender(
        String correlationId,
        BigDecimal amount,
        String requestedAt,
        Long requestedAtSeconds
) {
    public static PaymentDtoSender from(Payment payment) {
        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        String formattedDate = nowUtc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"));
        return new PaymentDtoSender(
                payment.correlationId().toString(),
                payment.amount(),
                formattedDate,
                nowUtc.toEpochSecond()
        );
    }
}
