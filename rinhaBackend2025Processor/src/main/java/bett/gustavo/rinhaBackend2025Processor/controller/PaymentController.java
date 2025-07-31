package bett.gustavo.rinhaBackend2025Processor.controller;

import bett.gustavo.rinhaBackend2025Model.model.Payment;
import bett.gustavo.rinhaBackend2025Model.service.PaymentService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@RestController
public class PaymentController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/payments-summary")
    @ResponseBody
    public ResponseEntity getPaymentsSummary(
            @RequestParam("from") String from,
            @RequestParam("to") String to) {

        ZonedDateTime fromDate = parseFlexibleZonedDateTime(from);
        ZonedDateTime toDate = parseFlexibleZonedDateTime(to);

        return ResponseEntity.ok(paymentService.paymentSummaryFromBetweenTo(fromDate.toEpochSecond(), toDate.toEpochSecond()));
    }

    @PostMapping("/payments")
    @ResponseBody
    public ResponseEntity postPayments(@RequestBody Payment payment) {
        rabbitTemplate.convertAndSend("payments", payment);
        return ResponseEntity.ok("Payment received with success!");
    }

    @GetMapping("/payments-delete")
    public ResponseEntity deletePayments() {
        paymentService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    private ZonedDateTime parseFlexibleZonedDateTime(String input) {
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
