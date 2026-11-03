
package com.lumaledger.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RecurringPayment {

    private Long id;
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private String frequency;
    private LocalDate nextDueDate;
    private boolean active;
    private LocalDateTime createdAt;

    public RecurringPayment() {
    }

    public RecurringPayment(
            Long categoryId,
            String title,
            BigDecimal amount,
            String frequency,
            LocalDate nextDueDate,
            boolean active) {
        this.categoryId = categoryId;
        this.title = title;
        this.amount = amount;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.active = active;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
