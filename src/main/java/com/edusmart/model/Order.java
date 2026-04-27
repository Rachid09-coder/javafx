package com.edusmart.model;

import java.time.LocalDateTime;

/**
 * Maps to the {@code orders} table.
 */
public class Order {
    private int id;
    private int studentId;
    private Integer promoCodeId;
    private double totalBeforeDiscount;
    private double discountAmount;
    private double totalAfterDiscount;
    private String status;
    private String stripeSessionId;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getPromoCodeId() { return promoCodeId; }
    public void setPromoCodeId(Integer promoCodeId) { this.promoCodeId = promoCodeId; }

    public double getTotalBeforeDiscount() { return totalBeforeDiscount; }
    public void setTotalBeforeDiscount(double totalBeforeDiscount) { this.totalBeforeDiscount = totalBeforeDiscount; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getTotalAfterDiscount() { return totalAfterDiscount; }
    public void setTotalAfterDiscount(double totalAfterDiscount) { this.totalAfterDiscount = totalAfterDiscount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStripeSessionId() { return stripeSessionId; }
    public void setStripeSessionId(String stripeSessionId) { this.stripeSessionId = stripeSessionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

