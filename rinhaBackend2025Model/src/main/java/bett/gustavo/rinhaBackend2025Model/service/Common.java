package bett.gustavo.rinhaBackend2025Model.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public class Common {
    public static final String PAYMENT_QUEUE = "paymentQueue";

    public static ZonedDateTime parseFlexibleZonedDateTime(String input) {
        if (input == null || input.isEmpty()) {
            return ZonedDateTime.now(ZoneOffset.UTC);
        }

        try {
            return ZonedDateTime.parse(input);
        } catch (DateTimeParseException e1) {
            try {
                LocalDateTime local = LocalDateTime.parse(input);
                return local.atZone(ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Formato de data inválido: " + input);
            }
        }
    }
}
