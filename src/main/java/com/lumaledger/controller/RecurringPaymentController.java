
package com.lumaledger.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lumaledger.model.RecurringPayment;
import com.lumaledger.service.RecurringPaymentService;

@RestController
@RequestMapping("/api/recurring-payments")
public class RecurringPaymentController {

    private final RecurringPaymentService recurringPaymentService;

    public RecurringPaymentController(
            RecurringPaymentService recurringPaymentService) {
        this.recurringPaymentService = recurringPaymentService;
    }

    @PostMapping
    public ResponseEntity<RecurringPayment> createPayment(
            @RequestBody RecurringPayment payment) {

        RecurringPayment created =
                recurringPaymentService.createPayment(payment);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RecurringPayment>> getAllPayments() {
        return ResponseEntity.ok(
                recurringPaymentService.getAllPayments());
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecurringPayment>> getActivePayments() {
        return ResponseEntity.ok(
                recurringPaymentService.getActivePayments());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<RecurringPayment>> getUpcomingPayments(
            @RequestParam(defaultValue = "30") int days) {

        return ResponseEntity.ok(
                recurringPaymentService.getUpcomingPayments(days));
    }

    @GetMapping("/upcoming/total")
    public ResponseEntity<BigDecimal> getUpcomingPaymentTotal(
            @RequestParam(defaultValue = "30") int days) {

        return ResponseEntity.ok(
                recurringPaymentService.getUpcomingPaymentTotal(days));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringPayment> getPaymentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                recurringPaymentService.getPaymentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updatePayment(
            @PathVariable Long id,
            @RequestBody RecurringPayment payment) {

        payment.setId(id);

        boolean updated =
                recurringPaymentService.updatePayment(payment);

        if (updated) {
            return ResponseEntity.ok(
                    "Recurring payment updated successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Recurring payment not found.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePayment(
            @PathVariable Long id) {

        boolean deleted =
                recurringPaymentService.deletePayment(id);

        if (deleted) {
            return ResponseEntity.ok(
                    "Recurring payment deleted successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Recurring payment not found.");
    }
}
