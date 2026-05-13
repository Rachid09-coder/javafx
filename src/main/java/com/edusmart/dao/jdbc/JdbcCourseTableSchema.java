package com.edusmart.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Detects {@code module_id} on the course table using INFORMATION_SCHEMA (reliable on MySQL),
 * with a metadata fallback if schema access fails.
 */
public final class JdbcCourseTableSchema {

    private static volatile boolean moduleIdColumnConfirmed;
    private static volatile boolean courseColumnsChecked;

    private JdbcCourseTableSchema() {}

    public static void ensureCourseColumns(Connection connection) {
        if (courseColumnsChecked) {
            return;
        }
        synchronized (JdbcCourseTableSchema.class) {
            if (courseColumnsChecked) {
                return;
            }
            ensureColumnExists(connection, "description", "TEXT NULL");
            ensureColumnExists(connection, "price", "DOUBLE NOT NULL DEFAULT 0");
            ensureColumnExists(connection, "status", "VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'");
            ensureColumnExists(connection, "created_at", "DATETIME NULL");
            ensureColumnExists(connection, "thumbnail_path", "VARCHAR(500) NULL");
            ensureColumnExists(connection, "pdf_path", "VARCHAR(500) NULL");
            ensureColumnExists(connection, "generated_content", "LONGTEXT NULL");
            ensureColumnExists(connection, "coefficient", "DOUBLE NULL");
            ensureColumnExists(connection, "module_id", "INT NULL");
            courseColumnsChecked = true;
        }
    }

    public static boolean tableExists(Connection connection, String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException ignored) {
            return false;
        }
    }

    public static boolean columnExists(Connection connection, String tableName, String columnName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(?) AND LOWER(COLUMN_NAME) = LOWER(?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException ignored) {
            return false;
        }
    }

    private static void ensureColumnExists(Connection connection, String columnName, String definition) {
        if (columnExists(connection, "course", columnName)) {
            return;
        }
        String sql = "ALTER TABLE course ADD COLUMN " + columnName + " " + definition;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.executeUpdate();
            if ("module_id".equalsIgnoreCase(columnName)) {
                moduleIdColumnConfirmed = true;
            }
        } catch (SQLException ex) {
            System.err.println("Warning: Could not verify/add course." + columnName + ": " + ex.getMessage());
        }
    }

    public static Integer findModuleIdColumnIndex(ResultSetMetaData meta) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnMatchesModuleId(meta.getColumnLabel(i)) || columnMatchesModuleId(meta.getColumnName(i))) {
                return i;
            }
        }
        return null;
    }

    private static boolean columnMatchesModuleId(String raw) {
        if (raw == null) {
            return false;
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return false;
        }
        int dot = s.lastIndexOf('.');
        if (dot >= 0) {
            s = s.substring(dot + 1).trim();
        }
        return "module_id".equalsIgnoreCase(s);
    }

    /**
     * Live check: {@code course} (or common variants) has {@code module_id}.
     */
    public static boolean probeModuleIdColumn(Connection connection) throws SQLException {
        ensureCourseColumns(connection);
        if (informationSchemaHasModuleId(connection)) {
            return true;
        }
        return metadataFallbackHasModuleId(connection);
    }

    private static boolean informationSchemaHasModuleId(Connection connection) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() "
                + "AND LOWER(COLUMN_NAME) = 'module_id' "
                + "AND LOWER(TABLE_NAME) IN ('course', 'cours', 'courses')";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getLong(1) > 0;
        } catch (SQLException ignored) {
            return false;
        }
    }

    private static boolean metadataFallbackHasModuleId(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM course WHERE 1 = 0");
             ResultSet rs = ps.executeQuery()) {
            return findModuleIdColumnIndex(rs.getMetaData()) != null;
        }
    }

    public static boolean hasModuleIdColumn(Connection connection) throws SQLException {
        if (moduleIdColumnConfirmed) {
            return true;
        }
        synchronized (JdbcCourseTableSchema.class) {
            if (moduleIdColumnConfirmed) {
                return true;
            }
            boolean found = probeModuleIdColumn(connection);
            if (found) {
                moduleIdColumnConfirmed = true;
            }
            return found;
        }
    }

    public static void clearCache() {
        moduleIdColumnConfirmed = false;
        courseColumnsChecked = false;
    }
}
