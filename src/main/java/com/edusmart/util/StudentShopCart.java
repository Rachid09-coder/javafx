package com.edusmart.util;

import com.edusmart.model.CartItem;
import com.edusmart.model.Product;
import com.edusmart.model.PromoCode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Shared student cart (survives scene navigation in the same app session).
 */
public final class StudentShopCart {

    private static final StudentShopCart INSTANCE = new StudentShopCart();

    private final Map<Integer, CartItem> byProductId = new HashMap<>();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();
    private PromoCode appliedPromo;

    private StudentShopCart() {}

    public static StudentShopCart getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() { return items; }

    public PromoCode getAppliedPromo() { return appliedPromo; }
    public void setAppliedPromo(PromoCode promo) { this.appliedPromo = promo; }
    public void clearPromo() { this.appliedPromo = null; }

    public CartItem getLine(int productId) { return byProductId.get(productId); }

    public void addOne(Product product) {
        if (product == null) return;
        CartItem line = byProductId.get(product.getId());
        if (line == null) {
            if (product.getStock() <= 0) return;
            line = new CartItem(product, 1);
            byProductId.put(product.getId(), line);
            items.add(line);
        } else {
            if (line.getQuantity() + 1 > product.getStock()) return;
            line.setQuantity(line.getQuantity() + 1);
        }
    }

    public void setQuantity(Product product, int qty) {
        if (product == null) return;
        int next = Math.max(0, qty);
        CartItem line = byProductId.get(product.getId());
        if (next <= 0) {
            if (line != null) {
                byProductId.remove(product.getId());
                items.remove(line);
            }
            return;
        }
        if (next > product.getStock()) next = product.getStock();
        if (line == null) {
            line = new CartItem(product, next);
            byProductId.put(product.getId(), line);
            items.add(line);
        } else {
            line.setQuantity(next);
        }
    }

    public void removeLine(int productId) {
        CartItem line = byProductId.remove(productId);
        if (line != null) items.remove(line);
    }

    public void clear() {
        byProductId.clear();
        items.clear();
        appliedPromo = null;
    }

    public double getSubtotalEur() {
        return items.stream()
            .mapToDouble(ci -> ci.getProduct().getPrice() * ci.getQuantity())
            .sum();
    }

    public double computeDiscountEur() {
        PromoCode p = appliedPromo;
        if (p == null) return 0;
        double pct = Math.max(0, Math.min(100, p.getDiscountPercent()));
        return getSubtotalEur() * (pct / 100.0);
    }

    public double getTotalEur() {
        return Math.max(0, getSubtotalEur() - computeDiscountEur());
    }

    public long getTotalCentsEur() {
        return Math.round(getTotalEur() * 100.0);
    }

    public String formatMoneyEur(double value) {
        return String.format("%.2f €", value);
    }
}
