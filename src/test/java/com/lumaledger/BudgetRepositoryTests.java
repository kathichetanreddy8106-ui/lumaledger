
package com.lumaledger;

import com.lumaledger.model.Budget;
import com.lumaledger.model.Category;
import com.lumaledger.model.TransactionType;
import com.lumaledger.repository.BudgetRepository;
import com.lumaledger.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BudgetRepositoryTests {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void createFindUpdateAndDeleteBudget() {
        Category category = new Category();
        category.setName("Budget Test " + System.nanoTime());
        category.setType(TransactionType.EXPENSE);

        Category savedCategory = categoryRepository.create(category);
        Long budgetId = null;

        try {
            Budget budget = new Budget();
            budget.setCategoryId(savedCategory.getId());
            budget.setBudgetMonth(LocalDate.of(2026, 10, 1));
            budget.setAmount(new BigDecimal("5000.00"));

            Budget savedBudget = budgetRepository.create(budget);
            budgetId = savedBudget.getId();

            assertNotNull(budgetId);
            assertEquals(new BigDecimal("5000.00"),
                    savedBudget.getAmount());

            assertTrue(budgetRepository.findById(budgetId).isPresent());

            savedBudget.setAmount(new BigDecimal("6000.00"));
            assertTrue(budgetRepository.update(savedBudget));

            assertEquals(0, new BigDecimal("6000.00")
                    .compareTo(budgetRepository.findById(budgetId)
                            .orElseThrow().getAmount()));

            assertTrue(budgetRepository.deleteById(budgetId));
            assertFalse(budgetRepository.findById(budgetId).isPresent());
        } finally {
            if (budgetId != null) {
                budgetRepository.deleteById(budgetId);
            }
            categoryRepository.deleteById(savedCategory.getId());
        }
    }

    @Test
    void findByMonthReturnsMatchingBudget() {
        Category category = new Category();
        category.setName("Budget Month Test " + System.nanoTime());
        category.setType(TransactionType.EXPENSE);

        Category savedCategory = categoryRepository.create(category);
        Long budgetId = null;

        try {
            Budget budget = new Budget();
            budget.setCategoryId(savedCategory.getId());
            budget.setBudgetMonth(LocalDate.of(2026, 10, 1));
            budget.setAmount(new BigDecimal("2500.00"));

            Budget savedBudget = budgetRepository.create(budget);
            budgetId = savedBudget.getId();

            Long createdBudgetId = budgetId;

assertTrue(budgetRepository.findByMonth(
                Date.valueOf("2026-10-01"))
        .stream()
        .anyMatch(item -> createdBudgetId.equals(item.getId())));
        } finally {
            if (budgetId != null) {
                budgetRepository.deleteById(budgetId);
            }
            categoryRepository.deleteById(savedCategory.getId());
        }
    }
}
