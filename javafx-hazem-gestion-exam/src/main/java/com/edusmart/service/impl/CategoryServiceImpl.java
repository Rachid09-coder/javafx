package com.edusmart.service.impl;

import com.edusmart.dao.CategoryDao;
import com.edusmart.model.Category;
import com.edusmart.service.CategoryService;

import java.util.List;
import java.util.Optional;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    public boolean createCategory(Category category) {
        return categoryDao.create(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(int id) {
        return categoryDao.findById(id);
    }

    @Override
    public boolean updateCategory(Category category) {
        return categoryDao.update(category);
    }

    @Override
    public boolean deleteCategory(int id) {
        return categoryDao.delete(id);
    }

    @Override
    public boolean deleteCategoryAndProducts(int categoryId) {
        return categoryDao.deleteWithProducts(categoryId);
    }
}
