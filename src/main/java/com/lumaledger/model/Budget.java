
package com.lumaledger.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Budget {

    private Long id;
    private Long categoryId;
    private LocalDate budgetMonth;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public Budget() {
    }

    public Budget(Long id, Long categoryId, LocalDate budgetMonth,
                  BigDecimal amount, LocalDateTime createdAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.budgetMonth = budgetMonth;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDate getBudgetMonth() {
        return budgetMonth;
    }

    public void setBudgetMonth(LocalDate budgetMonth) {
        this.budgetMonth = budgetMonth;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
