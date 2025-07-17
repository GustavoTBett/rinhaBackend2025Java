package bett.gustavo.rinhaBackend2025Model.service;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    @Qualifier("redisTemplateZset")
    private RedisTemplate<String, UUID> redisTemplateZset;

    @Autowired
    @Qualifier("redisTemplatePayment")
    private RedisTemplate<String, Payment> redisTemplate;


    public List<Payment> findByCreatedAtBetweenAndSituation(ZonedDateTime from, ZonedDateTime to, SituationPayment situation) {
        Long fromSeconds = from.toEpochSecond();
        Long toSeconds = to.toEpochSecond();

        Set<UUID> ids = redisTemplateZset.opsForZSet().rangeByScore("payments:byDate", fromSeconds, toSeconds);

        List<Payment> payments = ids.stream()
                .map(id -> redisTemplate.opsForValue().get("payment:" + id))
                .filter(Objects::nonNull)
                .filter(p -> p.getSituation() == situation)
                .collect(Collectors.toList());
        return payments;
    }

    public Payment save(Payment payment) {
        redisTemplateZset.opsForZSet().add("payments:byDate", payment.getCorrelationId(), payment.getCreateAtSeconds());
        redisTemplate.opsForValue().set(payment.getCorrelationId(), payment);
        return paymentRepository.save(payment);
    }

    public Payment update(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deleteById(UUID uuid) {
        paymentRepository.deleteById(uuid);
    }

    public void deleteAll() {
        paymentRepository.deleteAll();
    }
}
