package com.edusmart.dao.jdbc;

import com.edusmart.dao.CategoryDao;
import com.edusmart.model.Category;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryDao implements CategoryDao {

    @Override
    public boolean create(Category category) {
        String sql = "INSERT INTO category (name, description, icon, color) VALUES (?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, category);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create category", ex);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM category ORDER BY id";
        List<Category> list = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch categories", ex);
        }
        return list;
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM category WHERE id = ?";

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
            throw new RuntimeException("Failed to fetch category by id", ex);
        }
    }

    @Override
    public boolean update(Category category) {
        String sql = "UPDATE category SET name = ?, description = ?, icon = ?, color = ? WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, category);
            ps.setInt(5, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update category", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM category WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete category", ex);
        }
    }

    @Override
    public boolean deleteWithProducts(int categoryId) {
        String deleteProducts = "DELETE FROM product WHERE category_id = ?";
        String deleteCategory = "DELETE FROM category WHERE id = ?";

        try (Connection connection = DbConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(deleteProducts)) {
                    ps.setInt(1, categoryId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = connection.prepareStatement(deleteCategory)) {
                    ps.setInt(1, categoryId);
                    boolean deleted = ps.executeUpdate() > 0;
                    if (!deleted) {
                        connection.rollback();
                        return false;
                    }
                    connection.commit();
                    return true;
                }
            } catch (SQLException ex) {
                connection.rollback();
                throw new RuntimeException("Failed to delete category and related products", ex);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete category and related products", ex);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setIcon(rs.getString("icon"));
        c.setColor(rs.getString("color"));
        return c;
    }

    private void fillStatement(PreparedStatement ps, Category c) throws SQLException {
        ps.setString(1, c.getName());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getIcon());
        ps.setString(4, c.getColor());
    }
}
