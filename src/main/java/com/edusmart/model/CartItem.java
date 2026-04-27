package com.edusmart.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.Objects;

/**
 * UI cart line item.
 */
public class CartItem {
    private final Product product;
    private final IntegerProperty quantity = new SimpleIntegerProperty(0);

    public CartItem(Product product, int quantity) {
        this.product = Objects.requireNonNull(product, "product");
        setQuantity(quantity);
    }

    public Product getProduct() { return product; }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int value) { quantity.set(Math.max(0, value)); }
    public IntegerProperty quantityProperty() { return quantity; }
}
