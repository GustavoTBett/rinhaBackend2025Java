package bett.gustavo.rinhaBackend2025Model.service;

import bett.gustavo.rinhaBackend2025Model.dto.PaymentSummaryDto;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.model.SituationPayment;
import bett.gustavo.rinhaBackend2025Model.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    @Qualifier("redisTemplateZset")
    private RedisTemplate<String, Payment> redisTemplateZset;

    public Map<String, PaymentSummaryDto> paymentSummaryFromBetweenTo(Long fromEpoch, Long toEpoch) {
        Set<Payment> paymentDefault = redisTemplateZset.opsForZSet().rangeByScore("paymentsDefault:byDate", fromEpoch, toEpoch);
        Set<Payment> paymentFallback = redisTemplateZset.opsForZSet().rangeByScore("paymentsFallback:byDate", fromEpoch, toEpoch);

        assert paymentDefault != null;
        PaymentSummaryDto paymentSummaryDtoDefault = new PaymentSummaryDto(paymentDefault.size(), paymentDefault.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        assert paymentFallback != null;
        PaymentSummaryDto paymentSummaryDtoFallback = new PaymentSummaryDto(paymentFallback.size(), paymentFallback.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<String, PaymentSummaryDto> response = new HashMap<>();
        response.put("default", paymentSummaryDtoDefault);
        response.put("fallback", paymentSummaryDtoFallback);

        return response;
    }

    public void saveDefault(Payment payment) {
        redisTemplateZset.opsForZSet().add("paymentsDefault:byDate", payment, payment.getCreateAtSeconds());
    }

    public void saveFallback(Payment payment) {
        redisTemplateZset.opsForZSet().add("paymentsFallback:byDate", payment, payment.getCreateAtSeconds());
    }

    public Payment update(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deleteById(UUID uuid) {
        paymentRepository.deleteById(uuid);
    }

    public void deleteAll() {
        redisTemplateZset.delete("payments:byDate");
        paymentRepository.deleteAll();
    }
}
