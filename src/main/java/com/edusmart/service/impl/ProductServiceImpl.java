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
        return productDao.update(product);
    }

    @Override
    public boolean deleteProduct(int id) {
        return productDao.delete(id);
    }
}
