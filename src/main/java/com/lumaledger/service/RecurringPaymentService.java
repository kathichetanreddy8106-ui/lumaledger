
package com.lumaledger.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lumaledger.model.RecurringPayment;
import com.lumaledger.repository.RecurringPaymentRepository;

@Service
public class RecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;

    public RecurringPaymentService(
            RecurringPaymentRepository recurringPaymentRepository) {
        this.recurringPaymentRepository = recurringPaymentRepository;
    }

    public RecurringPayment createPayment(RecurringPayment payment) {

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Recurring payment cannot be null.");
        }

        if (payment.getTitle() == null || payment.getTitle().isBlank()) {
            throw new IllegalArgumentException("Payment title is required.");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }

        if (payment.getFrequency() == null
                || payment.getFrequency().isBlank()) {
            throw new IllegalArgumentException(
                    "Payment frequency is required.");
        }

        if (payment.getNextDueDate() == null) {
            throw new IllegalArgumentException(
                    "Next due date is required.");
        }

        String frequency = payment.getFrequency().trim().toUpperCase();

        if (!List.of("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
                .contains(frequency)) {
            throw new IllegalArgumentException(
                    "Frequency must be DAILY, WEEKLY, MONTHLY, or YEARLY.");
        }

        payment.setFrequency(frequency);

        return recurringPaymentRepository.create(payment);
    }

    public List<RecurringPayment> getAllPayments() {
        return recurringPaymentRepository.findAll();
    }

    public List<RecurringPayment> getActivePayments() {
        return recurringPaymentRepository.findActive();
    }

    public List<RecurringPayment> getUpcomingPayments(int days) {

        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative.");
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        return getActivePayments().stream()
                .filter(payment -> payment.getNextDueDate() != null)
                .filter(payment ->
                        !payment.getNextDueDate().isBefore(today)
                        && !payment.getNextDueDate().isAfter(endDate))
                .toList();
    }

    public BigDecimal getUpcomingPaymentTotal(int days) {

        if (days < 0) {
            throw new IllegalArgumentException("Days cannot be negative.");
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        BigDecimal total = BigDecimal.ZERO;

        for (RecurringPayment payment : getActivePayments()) {

            LocalDate dueDate = payment.getNextDueDate();
            BigDecimal amount = payment.getAmount();
            String frequency = payment.getFrequency();

            if (dueDate == null || amount == null || frequency == null) {
                continue;
            }

            while (dueDate.isBefore(today)) {
                dueDate = getNextDueDate(dueDate, frequency);
            }

            while (!dueDate.isAfter(endDate)) {
                total = total.add(amount);
                dueDate = getNextDueDate(dueDate, frequency);
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private LocalDate getNextDueDate(
            LocalDate currentDate, String frequency) {

        return switch (frequency.toUpperCase()) {
            case "DAILY" -> currentDate.plusDays(1);
            case "WEEKLY" -> currentDate.plusWeeks(1);
            case "MONTHLY" -> currentDate.plusMonths(1);
            case "YEARLY" -> currentDate.plusYears(1);
            default -> throw new IllegalArgumentException(
                    "Unsupported payment frequency: " + frequency);
        };
    }

    public RecurringPayment getPaymentById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Payment ID cannot be null.");
        }

        return recurringPaymentRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recurring payment not found."));
    }

    public boolean updatePayment(RecurringPayment payment) {

        if (payment == null || payment.getId() == null) {
            throw new IllegalArgumentException(
                    "Valid recurring payment is required.");
        }

        if (payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }

        if (payment.getNextDueDate() == null) {
            throw new IllegalArgumentException(
                    "Next due date is required.");
        }

        if (payment.getFrequency() == null) {
            throw new IllegalArgumentException(
                    "Payment frequency is required.");
        }

        String frequency = payment.getFrequency().trim().toUpperCase();

        if (!List.of("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
                .contains(frequency)) {
            throw new IllegalArgumentException(
                    "Invalid payment frequency.");
        }

        payment.setFrequency(frequency);

        return recurringPaymentRepository.update(payment);
    }

    public boolean deletePayment(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Payment ID cannot be null.");
        }

        return recurringPaymentRepository.deleteById(id);
    }
}
