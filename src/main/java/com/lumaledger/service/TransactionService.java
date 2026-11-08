
package com.lumaledger.service;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.TransactionRepository;
import com.lumaledger.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AuditLogRepository auditLogRepository) {
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        validateTransaction(transaction);
        transaction.setId(null);

        Transaction created = transactionRepository.create(transaction);

        auditLogRepository.save("CREATE", created);

        return created;
    }

    public Transaction getById(Long id) {
        validateId(id);

        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @Transactional
    public Transaction update(Long id, Transaction transaction) {
        validateId(id);
        validateTransaction(transaction);

        transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        transaction.setId(id);

        if (!transactionRepository.update(id, transaction)) {
            throw new TransactionNotFoundException(id);
        }

        Transaction updated = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        auditLogRepository.save("UPDATE", updated);

        return updated;
    }

    @Transactional
    public void delete(Long id) {
        validateId(id);

        Transaction existing = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        auditLogRepository.save("DELETE", existing);

        if (!transactionRepository.deleteById(id)) {
            throw new TransactionNotFoundException(id);
        }
    }

    public List<Transaction> search(
            LocalDate from,
            LocalDate to,
            TransactionType type,
            String description
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

        return transactionRepository.search(from, to, type, description);
    }

    public BigDecimal getMonthlyTotal(
            TransactionType type,
            int year,
            int month
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required"
            );
        }

        if (year < 1 || month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Invalid year or month"
            );
        }

        return transactionRepository.getMonthlyTotal(type, year, month);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Transaction ID must be a positive number"
            );
        }
    }

    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction details are required"
            );
        }

        if (transaction.getType() == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required"
            );
        }

        if (transaction.getAmount() == null
                || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }

        if (transaction.getAmount().scale() > 2) {
            throw new IllegalArgumentException(
                    "Amount cannot have more than two decimal places"
            );
        }

        if (transaction.getTransactionDate() == null) {
            throw new IllegalArgumentException(
                    "Transaction date is required"
            );
        }

        if (transaction.getDescription() != null
                && transaction.getDescription().length() > 500) {
            throw new IllegalArgumentException(
                    "Description cannot exceed 500 characters"
            );
        }
    }
}
