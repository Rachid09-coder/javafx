package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Maps to the {@code promo_code} table.
 */
public class PromoCode {
    private int id;
    private String code;
    private double discountPercent;
    private boolean active;
    private LocalDateTime createdAt;

    public PromoCode() {}

    public PromoCode(int id, String code, double discountPercent, boolean active) {
        this.id = id;
        this.code = code;
        this.discountPercent = discountPercent;
        this.active = active;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

