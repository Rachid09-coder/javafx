package com.edusmart.service;

import com.edusmart.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    boolean createCategory(Category category);
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(int id);
    boolean updateCategory(Category category);
    boolean deleteCategory(int id);

    /** Deletes all products in this category, then the category (one transaction). */
    boolean deleteCategoryAndProducts(int categoryId);
}
