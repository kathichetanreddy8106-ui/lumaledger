package com.lumaledger.controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lumaledger.model.Budget;
import com.lumaledger.service.BudgetService;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public Budget createBudget(@RequestBody Budget budget) {
        return budgetService.createBudget(budget);
    }

    @GetMapping
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("/monthly")
    public List<Budget> getBudgetsByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return budgetService.getBudgetsByMonth(
                YearMonth.of(year, month));
    }

    @GetMapping("/{id}")
    public Budget getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @DeleteMapping("/{id}")
    public boolean deleteBudget(@PathVariable Long id) {
        return budgetService.deleteBudget(id);
    }
}