
package com.lumaledger.controller;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@Valid @RequestBody Transaction transaction) {
        return transactionService.create(transaction);
    }

    @GetMapping("/{id}")
    public Transaction getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping
    public List<Transaction> getAll() {
        return transactionService.getAll();
    }

    @PutMapping("/{id}")
    public Transaction update(
            @PathVariable Long id,
            @Valid @RequestBody Transaction transaction
    ) {
        return transactionService.update(id, transaction);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }

    @GetMapping("/search")
    public List<Transaction> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @RequestParam(required = false) TransactionType type,

            @RequestParam(required = false) String description
    ) {
        return transactionService.search(from, to, type, description);
    }

    @GetMapping("/monthly-total")
    public BigDecimal getMonthlyTotal(
            @RequestParam TransactionType type,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return transactionService.getMonthlyTotal(type, year, month);
    }
}
