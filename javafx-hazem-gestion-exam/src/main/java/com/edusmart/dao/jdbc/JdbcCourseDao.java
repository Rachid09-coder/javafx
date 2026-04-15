package com.edusmart.dao.jdbc;

import com.edusmart.dao.CourseDao;
import com.edusmart.model.Course;
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

public class JdbcCourseDao implements CourseDao {

    private static final String COURSE_COLUMNS_BASE = "id, title, description, price, status, created_at, thumbnail_path, pdf_path, generated_content, coefficient";
    private static final String COURSE_SELECT_WITH_MODULE = "SELECT " + COURSE_COLUMNS_BASE + ", module_id FROM course ";
    private static final String COURSE_SELECT_WITHOUT_MODULE = "SELECT " + COURSE_COLUMNS_BASE + " FROM course ";

    @Override
    public boolean create(Course course) {
        try (Connection connection = DbConnection.getConnection()) {
            if (JdbcCourseTableSchema.hasModuleIdColumn(connection)) {
                String sql = "INSERT INTO course (title, description, price, status, created_at, thumbnail_path, pdf_path, generated_content, coefficient, module_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    fillCreateStatementWithModule(ps, course);
                    return ps.executeUpdate() > 0;
                }
            } else {
                String sql = "INSERT INTO course (title, description, price, status, created_at, thumbnail_path, pdf_path, generated_content, coefficient) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    fillCreateStatementWithoutModule(ps, course);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create course", ex);
        }
    }

    @Override
    public List<Course> findAll() {
        try (Connection connection = DbConnection.getConnection()) {
            boolean hasModule = JdbcCourseTableSchema.probeModuleIdColumn(connection);
            String sql = (hasModule ? COURSE_SELECT_WITH_MODULE : COURSE_SELECT_WITHOUT_MODULE) + "ORDER BY id";
            List<Course> courses = new ArrayList<>();
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapRow(rs, hasModule));
                }
            }
            return courses;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch courses", ex);
        }
    }

    @Override
    public Optional<Course> findById(int id) {
        try (Connection connection = DbConnection.getConnection()) {
            boolean hasModule = JdbcCourseTableSchema.probeModuleIdColumn(connection);
            String sql = (hasModule ? COURSE_SELECT_WITH_MODULE : COURSE_SELECT_WITHOUT_MODULE) + "WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(mapRow(rs, hasModule));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch course by id", ex);
        }
    }

    @Override
    public boolean update(Course course) {
        try (Connection connection = DbConnection.getConnection()) {
            if (JdbcCourseTableSchema.hasModuleIdColumn(connection)) {
                String sql = "UPDATE course SET title = ?, description = ?, price = ?, status = ?, created_at = ?, thumbnail_path = ?, pdf_path = ?, generated_content = ?, coefficient = ?, module_id = ? "
                        + "WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    fillUpdateCommon(ps, course);
                    if (course.getModuleId() != null) {
                        ps.setInt(10, course.getModuleId());
                    } else {
                        ps.setNull(10, Types.INTEGER);
                    }
                    ps.setInt(11, course.getId());
                    return ps.executeUpdate() > 0;
                }
            } else {
                String sql = "UPDATE course SET title = ?, description = ?, price = ?, status = ?, created_at = ?, thumbnail_path = ?, pdf_path = ?, generated_content = ?, coefficient = ? "
                        + "WHERE id = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    fillUpdateCommon(ps, course);
                    ps.setInt(10, course.getId());
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update course", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM course WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete course", ex);
        }
    }

    private void fillUpdateCommon(PreparedStatement ps, Course course) throws SQLException {
        ps.setString(1, course.getTitle());
        ps.setString(2, course.getDescription());
        ps.setDouble(3, course.getPrice());
        ps.setString(4, course.getStatusValue());
        ps.setTimestamp(5, Timestamp.valueOf(course.getCreatedAt()));
        ps.setString(6, course.getThumbnailPath());
        ps.setString(7, course.getPdfPath());
        ps.setString(8, course.getGeneratedContent());
        if (course.getCoefficient() != null) {
            ps.setDouble(9, course.getCoefficient());
        } else {
            ps.setNull(9, Types.DOUBLE);
        }
    }

    private Course mapRow(ResultSet rs, boolean includeModuleId) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setPrice(rs.getDouble("price"));
        course.setStatusValue(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        course.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        course.setThumbnailPath(rs.getString("thumbnail_path"));
        course.setPdfPath(rs.getString("pdf_path"));
        course.setGeneratedContent(rs.getString("generated_content"));
        double coefficient = rs.getDouble("coefficient");
        course.setCoefficient(rs.wasNull() ? null : coefficient);
        if (includeModuleId) {
            Object mid = rs.getObject("module_id");
            if (mid == null) {
                course.setModuleId(null);
            } else if (mid instanceof Number) {
                course.setModuleId(((Number) mid).intValue());
            } else {
                course.setModuleId(Integer.parseInt(mid.toString()));
            }
        } else {
            course.setModuleId(null);
        }
        return course;
    }

    private void fillCreateStatementWithModule(PreparedStatement ps, Course course) throws SQLException {
        fillCreateStatementWithoutModule(ps, course);
        if (course.getModuleId() != null) {
            ps.setInt(10, course.getModuleId());
        } else {
            ps.setNull(10, Types.INTEGER);
        }
    }

    private void fillCreateStatementWithoutModule(PreparedStatement ps, Course course) throws SQLException {
        ps.setString(1, course.getTitle());
        ps.setString(2, course.getDescription());
        ps.setDouble(3, course.getPrice());
        ps.setString(4, course.getStatusValue());
        ps.setTimestamp(5, Timestamp.valueOf(course.getCreatedAt()));
        ps.setString(6, course.getThumbnailPath());
        ps.setString(7, course.getPdfPath());
        ps.setString(8, course.getGeneratedContent());
        if (course.getCoefficient() != null) {
            ps.setDouble(9, course.getCoefficient());
        } else {
            ps.setNull(9, Types.DOUBLE);
        }
    }
}
