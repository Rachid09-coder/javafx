package com.edusmart.util;

import com.edusmart.model.Product;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StudentShopCart {

    private static final Map<Product, Integer> ITEMS = new LinkedHashMap<>();

    private StudentShopCart() {
    }

    public static synchronized void put(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }
        ITEMS.put(product, quantity);
    }

    public static synchronized void remove(Product product) {
        if (product != null) {
            ITEMS.remove(product);
        }
    }

    public static synchronized void clear() {
        ITEMS.clear();
    }

    public static synchronized void replaceWith(Map<Product, Integer> source) {
        ITEMS.clear();
        if (source != null) {
            ITEMS.putAll(source);
        }
    }

    public static synchronized Map<Product, Integer> snapshot() {
        return new LinkedHashMap<>(ITEMS);
    }

    public static synchronized double getTotal() {
        return ITEMS.entrySet().stream()
            .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
            .sum();
    }

    public static synchronized String buildSummary() {
        if (ITEMS.isEmpty()) {
            return "Aucun article dans le panier.";
        }

        StringBuilder builder = new StringBuilder();
        ITEMS.forEach((product, quantity) -> builder
            .append(product.getName())
            .append(" x")
            .append(quantity)
            .append(" = ")
            .append(String.format("%.2f €", product.getPrice() * quantity))
            .append('\n'));
        builder.append("Total: ").append(String.format("%.2f €", getTotal()));
        return builder.toString();
    }
}