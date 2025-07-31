package bett.gustavo.rinhaBackend2025Api.controller;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.service.Common;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    @Qualifier("reactiveRedisTemplatePayment")
    private ReactiveRedisTemplate<String, Payment> reactiveRedisTemplate;

    @GetMapping("/payments-summary")
    @ResponseBody
    public ResponseEntity getPaymentsSummary(
            @RequestParam("from") String from,
            @RequestParam("to") String to) {

        ZonedDateTime fromDate = Common.parseFlexibleZonedDateTime(from);
        ZonedDateTime toDate = Common.parseFlexibleZonedDateTime(to);

        return ResponseEntity.ok(paymentService.paymentSummaryFromBetweenTo(fromDate.toEpochSecond(), toDate.toEpochSecond()));
    }

    @PostMapping("/payments")
    @ResponseBody
    public ResponseEntity postPayments(@RequestBody Payment payment) {
        reactiveRedisTemplate.convertAndSend(Common.PAYMENT_QUEUE, payment);
        return ResponseEntity.ok("Payment received with success!");
    }

    @GetMapping("/payments-delete")
    public ResponseEntity deletePayments() {
        paymentService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
