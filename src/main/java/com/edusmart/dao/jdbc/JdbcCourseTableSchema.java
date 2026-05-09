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

    private JdbcCourseTableSchema() {}

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
    }
}
