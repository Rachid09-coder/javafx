package com.edusmart.dao.jdbc;

import com.edusmart.dao.ProductDao;
import com.edusmart.model.Product;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductDao implements ProductDao {

    @Override
    public boolean create(Product product) {
        String sql = "INSERT INTO product (name, price, stock, image, category_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, product);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create product", ex);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM product ORDER BY id";
        List<Product> list = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch products", ex);
        }
        return list;
    }

    @Override
    public int countByCategoryId(int categoryId) {
        String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to count products by category", ex);
        }
    }

    @Override
    public Optional<Product> findById(int id) {
        String sql = "SELECT * FROM product WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch product by id", ex);
        }
    }

    @Override
    public boolean update(Product product) {
        String sql = "UPDATE product SET name = ?, price = ?, stock = ?, image = ?, category_id = ? WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, product);
            ps.setInt(6, product.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update product", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM product WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete product", ex);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getBigDecimal("price").doubleValue());
        p.setStock(rs.getInt("stock"));
        p.setImage(rs.getString("image"));
        p.setCategoryId(rs.getInt("category_id"));
        return p;
    }

    private void fillStatement(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setBigDecimal(2, java.math.BigDecimal.valueOf(p.getPrice()));
        ps.setInt(3, p.getStock());
        if (p.getImage() != null && !p.getImage().isBlank()) {
            ps.setString(4, p.getImage().trim());
        } else {
            ps.setNull(4, Types.VARCHAR);
        }
        ps.setInt(5, p.getCategoryId());
    }
}
