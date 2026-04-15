package com.edusmart.dao;

import com.edusmart.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryDao {
    boolean create(Category category);
    List<Category> findAll();
    Optional<Category> findById(int id);
    boolean update(Category category);
    boolean delete(int id);

    /**
     * Deletes all rows in {@code product} with this {@code category_id}, then the category row.
     * Runs in a single DB transaction.
     */
    boolean deleteWithProducts(int categoryId);
}
