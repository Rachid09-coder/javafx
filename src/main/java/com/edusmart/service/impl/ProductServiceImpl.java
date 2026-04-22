package com.edusmart.service.impl;

import com.edusmart.dao.ProductDao;
import com.edusmart.model.Product;
import com.edusmart.service.ProductService;

import java.util.List;
import java.util.Optional;

public class ProductServiceImpl implements ProductService {

    private final ProductDao productDao;

    public ProductServiceImpl(ProductDao productDao) {
        this.productDao = productDao;
    }

    @Override
    public boolean createProduct(Product product) {
        validateProduct(product);
        return productDao.create(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    @Override
    public Optional<Product> getProductById(int id) {
        return productDao.findById(id);
    }

    @Override
    public int countProductsByCategory(int categoryId) {
        return productDao.countByCategoryId(categoryId);
    }

    @Override
    public boolean updateProduct(Product product) {
        validateProduct(product);
        return productDao.update(product);
    }

    @Override
    public boolean deleteProduct(int id) {
        return productDao.delete(id);
    }

    // ── Validation métier ─────────────────────────────────────────────────
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du produit est obligatoire.");
        }
        if (product.getName().trim().length() < 2) {
            throw new IllegalArgumentException("Le nom doit contenir au moins 2 caractères.");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif.");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Le stock ne peut pas être négatif.");
        }
        if (product.getCategoryId() <= 0) {
            throw new IllegalArgumentException("La catégorie est obligatoire.");
        }
    }
}
