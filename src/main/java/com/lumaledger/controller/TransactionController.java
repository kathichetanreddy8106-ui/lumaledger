
package com.lumaledger.controller;

import com.lumaledger.csv.CSVImporter;
import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.service.TransactionService;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CSVImporter csvImporter;

    public TransactionController(
            TransactionService transactionService,
            CSVImporter csvImporter
    ) {
        this.transactionService = transactionService;
        this.csvImporter = csvImporter;
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

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions() {

        List<Transaction> transactions = transactionService.getAll();

        StringBuilder csv = new StringBuilder();

        csv.append("id,categoryId,type,amount,description,transactionDate\n");

        for (Transaction transaction : transactions) {
            csv.append(transaction.getId()).append(",");
            csv.append(transaction.getCategoryId() == null
                    ? ""
                    : transaction.getCategoryId()).append(",");
            csv.append(transaction.getType()).append(",");
            csv.append(transaction.getAmount()).append(",");

            String description = transaction.getDescription() == null
                    ? ""
                    : transaction.getDescription();

            csv.append("\"")
                    .append(description.replace("\"", "\"\""))
                    .append("\"")
                    .append(",");

            csv.append(transaction.getTransactionDate()).append("\n");
        }

        byte[] csvBytes = csv.toString()
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.csv"
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PostMapping(
            value = "/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> importTransactions(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Please upload a CSV file.");
        }

        int importedCount =
                csvImporter.importTransactions(file.getInputStream());

        return ResponseEntity.ok(
                importedCount + " transactions imported successfully."
        );
    }
}
