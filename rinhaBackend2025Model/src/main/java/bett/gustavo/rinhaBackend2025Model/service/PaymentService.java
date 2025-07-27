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
    private RedisTemplate<String, UUID> redisTemplateZset;


    public Map<String, PaymentSummaryDto> paymentSummaryFromBetweenTo(ZonedDateTime from, ZonedDateTime to) {
        Long fromSeconds = from.toEpochSecond();
        Long toSeconds = to.toEpochSecond();

        Set<UUID> ids = redisTemplateZset.opsForZSet().rangeByScore("payments:byDate", fromSeconds, toSeconds);

        List<Optional<Payment>> optionalList = ids.stream()
                .filter(Objects::nonNull)
                .map(id -> paymentRepository.findById(id))
                .toList();

        List<Payment> defaults = new ArrayList<>();
        List<Payment> fallbacks = new ArrayList<>();

        for (Optional<Payment> optionalPayment : optionalList) {
            if (optionalPayment.isPresent()) {
                Payment payment = optionalPayment.get();
                if (payment.getSituation().equals(SituationPayment.DEFAULT)) {
                    defaults.add(payment);
                } else if (payment.getSituation().equals(SituationPayment.FALLBACK)) {
                    fallbacks.add(payment);
                }
            }
        }

        PaymentSummaryDto paymentSummaryDtoDefault = new PaymentSummaryDto(defaults.size(), defaults.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        PaymentSummaryDto paymentSummaryDtoFallback = new PaymentSummaryDto(fallbacks.size(), fallbacks.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Map<String, PaymentSummaryDto> response = new HashMap<>();
        response.put("default", paymentSummaryDtoDefault);
        response.put("fallback", paymentSummaryDtoFallback);

        return response;
    }

    public Payment save(Payment payment) {
        redisTemplateZset.opsForZSet().add("payments:byDate", payment.getCorrelationId(), payment.getCreateAtSeconds());
        return paymentRepository.save(payment);
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
