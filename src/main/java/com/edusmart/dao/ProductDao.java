package com.edusmart.dao;

import com.edusmart.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDao {
    boolean create(Product product);
    List<Product> findAll();
    Optional<Product> findById(int id);
    /** Number of products linked to this category (FK {@code product.category_id}). */
    int countByCategoryId(int categoryId);
    boolean update(Product product);
    boolean delete(int id);
}
