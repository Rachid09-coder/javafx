package com.edusmart.service;

import com.edusmart.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    boolean createProduct(Product product);
    List<Product> getAllProducts();
    Optional<Product> getProductById(int id);
    int countProductsByCategory(int categoryId);
    boolean updateProduct(Product product);
    boolean deleteProduct(int id);
}
