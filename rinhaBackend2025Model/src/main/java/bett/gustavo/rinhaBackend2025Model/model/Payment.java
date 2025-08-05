package bett.gustavo.rinhaBackend2025Model.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@RedisHash("payment")
public record Payment(
        @Id
        @Indexed
        UUID correlationId,
        BigDecimal amount,
        SituationPayment situation,
        String createdAt,
        Long createAtSeconds
) implements Serializable {

    public Payment(UUID correlationId, BigDecimal amount, String createdAt, Long createAtSeconds) {
        this(correlationId, amount, SituationPayment.QUEUE, createdAt, createAtSeconds);
    }

    public static Payment atualizaPayment(Payment payment, String createdAt, Long createAtSeconds, SituationPayment situation) {
        return new Payment(
                payment.correlationId(),
                payment.amount(),
                situation,
                createdAt,
                createAtSeconds
        );
    }
}