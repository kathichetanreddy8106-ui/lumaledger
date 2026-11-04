
package com.lumaledger.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lumaledger.model.Budget;
import com.lumaledger.repository.BudgetRepository;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget createBudget(Budget budget) {

        if (budget == null ||
                budget.getCategoryId() == null ||
                budget.getBudgetMonth() == null ||
                budget.getAmount() == null) {
            throw new IllegalArgumentException(
                    "Budget details cannot be empty."
            );
        }

        if (budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget amount must be greater than zero."
            );
        }

        // Normalize the budget date to the first day of its month.
        YearMonth month = YearMonth.from(budget.getBudgetMonth());
        budget.setBudgetMonth(month.atDay(1));

        return budgetRepository.create(budget);
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public List<Budget> getBudgetsByMonth(YearMonth month) {

        if (month == null) {
            throw new IllegalArgumentException(
                    "Budget month cannot be null."
            );
        }

        LocalDate firstDay = month.atDay(1);

        return budgetRepository.findByMonth(
                java.sql.Date.valueOf(firstDay)
        );
    }

    public Budget getBudgetById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "Budget ID cannot be null."
            );
        }

        return budgetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Budget not found."
                ));
    }

    public boolean deleteBudget(Long id) {
        return budgetRepository.deleteById(id);
    }
}
