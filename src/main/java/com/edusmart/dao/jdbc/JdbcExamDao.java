package com.edusmart.dao.jdbc;

import com.edusmart.dao.ExamDao;
import com.edusmart.model.Exam;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExamDao implements ExamDao {

    @Override
    public boolean create(Exam exam) {
        String sql = "INSERT INTO exam (title, description, type, file_path, external_link, duration, module_name, grade_category, academic_year, semester, coefficient, course_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillCreateOrUpdateStatement(ps, exam);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create exam", ex);
        }
    }

    @Override
    public List<Exam> findAll() {
        String sql = "SELECT * FROM exam ORDER BY id";
        List<Exam> exams = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                exams.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch exams", ex);
        }
        return exams;
    }

    @Override
    public Optional<Exam> findById(int id) {
        String sql = "SELECT * FROM exam WHERE id = ?";

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
            throw new RuntimeException("Failed to fetch exam by id", ex);
        }
    }

    @Override
    public boolean update(Exam exam) {
        String sql = "UPDATE exam SET title = ?, description = ?, type = ?, file_path = ?, external_link = ?, duration = ?, module_name = ?, grade_category = ?, academic_year = ?, semester = ?, coefficient = ?, course_id = ? " +
                "WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillCreateOrUpdateStatement(ps, exam);
            ps.setInt(13, exam.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update exam", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM exam WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete exam", ex);
        }
    }

    private Exam mapRow(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setId(rs.getInt("id"));
        exam.setTitle(rs.getString("title"));
        exam.setDescription(rs.getString("description"));
        exam.setType(rs.getString("type"));
        exam.setFilePath(rs.getString("file_path"));
        exam.setExternalLink(rs.getString("external_link"));

        int duration = rs.getInt("duration");
        exam.setDuration(rs.wasNull() ? null : duration);

        exam.setModuleName(rs.getString("module_name"));
        exam.setGradeCategory(rs.getString("grade_category"));
        exam.setAcademicYear(rs.getString("academic_year"));

        int semester = rs.getInt("semester");
        exam.setSemester(rs.wasNull() ? null : semester);

        double coefficient = rs.getDouble("coefficient");
        exam.setCoefficient(rs.wasNull() ? null : coefficient);

        int courseId = rs.getInt("course_id");
        exam.setCourseIdNullable(rs.wasNull() ? null : courseId);
        return exam;
    }

    private void fillCreateOrUpdateStatement(PreparedStatement ps, Exam exam) throws SQLException {
        ps.setString(1, exam.getTitle());
        ps.setString(2, exam.getDescription());
        ps.setString(3, exam.getType());
        ps.setString(4, exam.getFilePath());
        ps.setString(5, exam.getExternalLink());

        if (exam.getDuration() != null) {
            ps.setInt(6, exam.getDuration());
        } else {
            ps.setNull(6, java.sql.Types.INTEGER);
        }

        ps.setString(7, exam.getModuleName());
        ps.setString(8, exam.getGradeCategory());
        ps.setString(9, exam.getAcademicYear());

        if (exam.getSemester() != null) {
            ps.setInt(10, exam.getSemester());
        } else {
            ps.setNull(10, java.sql.Types.INTEGER);
        }

        if (exam.getCoefficient() != null) {
            ps.setDouble(11, exam.getCoefficient());
        } else {
            ps.setNull(11, java.sql.Types.DOUBLE);
        }

        if (exam.getCourseIdNullable() != null) {
            ps.setInt(12, exam.getCourseIdNullable());
        } else {
            ps.setNull(12, java.sql.Types.INTEGER);
        }
    }
}
