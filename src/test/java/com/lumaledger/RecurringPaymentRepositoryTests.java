
package com.lumaledger;

import com.lumaledger.model.RecurringPayment;
import com.lumaledger.repository.RecurringPaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecurringPaymentRepositoryTests {

    @Autowired
    private RecurringPaymentRepository recurringPaymentRepository;

    @Test
    void shouldCreateFindUpdateAndDeleteRecurringPayment() {
        RecurringPayment payment = new RecurringPayment(
                null,
                "Test Internet Subscription",
                new BigDecimal("799.00"),
                "MONTHLY",
                LocalDate.of(2026, 11, 15),
                true
        );

        RecurringPayment created =
                recurringPaymentRepository.create(payment);

        assertNotNull(created.getId());
        assertEquals("Test Internet Subscription", created.getTitle());
        assertEquals(0,
                new BigDecimal("799.00").compareTo(created.getAmount()));

        Long id = created.getId();

        assertTrue(recurringPaymentRepository.findById(id).isPresent());

        created.setTitle("Updated Internet Subscription");
        created.setAmount(new BigDecimal("899.00"));

        assertTrue(recurringPaymentRepository.update(created));

        RecurringPayment updated =
                recurringPaymentRepository.findById(id).orElseThrow();

        assertEquals("Updated Internet Subscription", updated.getTitle());
        assertEquals(0,
                new BigDecimal("899.00").compareTo(updated.getAmount()));

        assertTrue(recurringPaymentRepository.deleteById(id));
        assertFalse(recurringPaymentRepository.findById(id).isPresent());
    }

    @Test
    void shouldFindActivePaymentsDueOnOrBeforeDate() {
        RecurringPayment payment = new RecurringPayment(
                null,
                "Test Due Payment",
                new BigDecimal("250.00"),
                "MONTHLY",
                LocalDate.of(2026, 10, 1),
                true
        );

        RecurringPayment created =
                recurringPaymentRepository.create(payment);

        try {
            assertTrue(
                    recurringPaymentRepository
                            .findDueOnOrBefore(Date.valueOf("2026-10-31"))
                            .stream()
                            .anyMatch(item -> created.getId().equals(item.getId()))
            );
        } finally {
            recurringPaymentRepository.deleteById(created.getId());
        }
    }
}
