package com.edusmart.dao.jdbc;

import com.edusmart.dao.ModuleDao;
import com.edusmart.model.Module;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcModuleDao implements ModuleDao {

    @Override
    public boolean create(Module module) {
        String sql = "INSERT INTO module (title, description, thumbnail, created_at) VALUES (?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillCreateStatement(ps, module);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create module", ex);
        }
    }

    @Override
    public List<Module> findAll() {
        String sql = "SELECT * FROM module ORDER BY id";
        List<Module> modules = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modules.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch modules", ex);
        }
        return modules;
    }

    @Override
    public Optional<Module> findById(int id) {
        String sql = "SELECT * FROM module WHERE id = ?";

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
            throw new RuntimeException("Failed to fetch module by id", ex);
        }
    }

    @Override
    public boolean update(Module module) {
        String sql = "UPDATE module SET title = ?, description = ?, thumbnail = ?, created_at = ? WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, module.getTitle());
            if (module.getDescription() != null && !module.getDescription().isBlank()) {
                ps.setString(2, module.getDescription());
            } else {
                ps.setNull(2, Types.LONGVARCHAR);
            }
            if (module.getThumbnail() != null && !module.getThumbnail().isBlank()) {
                ps.setString(3, module.getThumbnail().trim());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            if (module.getCreatedAt() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(module.getCreatedAt()));
            } else {
                ps.setNull(4, Types.TIMESTAMP);
            }
            ps.setInt(5, module.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update module", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection connection = DbConnection.getConnection()) {
            if (!JdbcCourseTableSchema.probeModuleIdColumn(connection)) {
                return deleteModuleRow(connection, id);
            }
            return deleteModuleCascade(connection, id);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete module and related courses", ex);
        }
    }

    private static boolean deleteModuleRow(Connection connection, int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM module WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private static boolean deleteModuleCascade(Connection connection, int id) throws SQLException {
        String deleteExamsLinkedToModuleCourses =
                "DELETE e FROM exam e INNER JOIN course c ON e.course_id = c.id WHERE c.module_id = ?";
        String deleteCourses = "DELETE FROM course WHERE module_id = ?";
        String deleteModule = "DELETE FROM module WHERE id = ?";

        connection.setAutoCommit(false);
        try {
            try (PreparedStatement ps = connection.prepareStatement(deleteExamsLinkedToModuleCourses)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(deleteCourses)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(deleteModule)) {
                ps.setInt(1, id);
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
            throw new RuntimeException("Failed to delete module and related courses", ex);
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private Module mapRow(ResultSet rs) throws SQLException {
        Module module = new Module();
        module.setId(rs.getInt("id"));
        module.setTitle(rs.getString("title"));
        module.setDescription(rs.getString("description"));
        module.setThumbnail(rs.getString("thumbnail"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        module.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        return module;
    }

    private void fillCreateStatement(PreparedStatement ps, Module module) throws SQLException {
        ps.setString(1, module.getTitle());
        if (module.getDescription() != null && !module.getDescription().isBlank()) {
            ps.setString(2, module.getDescription());
        } else {
            ps.setNull(2, Types.LONGVARCHAR);
        }
        if (module.getThumbnail() != null && !module.getThumbnail().isBlank()) {
            ps.setString(3, module.getThumbnail().trim());
        } else {
            ps.setNull(3, Types.VARCHAR);
        }
        if (module.getCreatedAt() != null) {
            ps.setTimestamp(4, Timestamp.valueOf(module.getCreatedAt()));
        } else {
            ps.setNull(4, Types.TIMESTAMP);
        }
    }
}
