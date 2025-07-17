package bett.gustavo.rinhaBackend2025Model.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@RedisHash(value = "payment")
public class Payment implements Serializable {

    @Id
    @Indexed
    private UUID correlationId;

    private BigDecimal amount;

    private SituationPayment situation = SituationPayment.QUEUE;

    private String createdAt;

    private Long createAtSeconds;
}
