
package com.lumaledger.csv;

import com.lumaledger.model.Transaction;
import com.lumaledger.model.TransactionType;
import com.lumaledger.service.TransactionService;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
public class CSVImporter {

    private final TransactionService transactionService;

    public CSVImporter(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public int importTransactions(InputStream inputStream) throws IOException {

        int importedCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",", -1);

                if (values.length < 6) {
                    continue;
                }

                Transaction transaction = new Transaction();

                transaction.setCategoryId(
                        values[1].trim().isEmpty()
                                ? null
                                : Long.parseLong(values[1].trim()));

                transaction.setType(
                        TransactionType.valueOf(values[2].trim()));

                transaction.setAmount(
                        new BigDecimal(values[3].trim()));

                transaction.setDescription(
                        values[4].trim().replace("\"", ""));

                transaction.setTransactionDate(
                        LocalDate.parse(values[5].trim()));

                transactionService.create(transaction);

                importedCount++;
            }
        }

        return importedCount;
    }
}
