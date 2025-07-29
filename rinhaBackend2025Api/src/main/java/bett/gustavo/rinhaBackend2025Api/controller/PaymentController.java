package bett.gustavo.rinhaBackend2025Api.controller;

import bett.gustavo.rinhaBackend2025Api.consumer.PaymentConsumer;
import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentConsumer paymentConsumer;

    private final String queueName = "paymentQueue";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/payments-summary")
    @ResponseBody
    public ResponseEntity getPaymentsSummary(
            @RequestParam("from") String from,
            @RequestParam("to") String to) {

        ZonedDateTime fromDate = parseFlexibleZonedDateTime(from);
        ZonedDateTime toDate = parseFlexibleZonedDateTime(to);

        return ResponseEntity.ok(paymentService.paymentSummaryFromBetweenTo(fromDate, toDate));
    }

    @PostMapping("/payments")
    @ResponseBody
    public ResponseEntity postPayments(@RequestBody Payment payment) throws JsonProcessingException {
        String json = new ObjectMapper().writeValueAsString(payment);
        redisTemplate.opsForList().leftPush(queueName, json);
        return ResponseEntity.ok("Payment received with success!");
    }

    @GetMapping("/payments-delete")
    public ResponseEntity deletePayments() {
        paymentService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private ZonedDateTime parseFlexibleZonedDateTime(String input) {
        if (input == null || input.isEmpty()) {
            return ZonedDateTime.now(ZoneOffset.UTC); // ou algum valor default
        }

        try {
            // Tenta com fuso horário primeiro
            return ZonedDateTime.parse(input);
        } catch (DateTimeParseException e1) {
            try {
                // Tenta como LocalDateTime e adiciona UTC como fuso
                LocalDateTime local = LocalDateTime.parse(input);
                return local.atZone(ZoneOffset.UTC); // ou ZoneId.of("America/Sao_Paulo")
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Formato de data inválido: " + input);
            }
        }
    }
}
