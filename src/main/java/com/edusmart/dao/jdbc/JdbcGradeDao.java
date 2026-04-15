package com.edusmart.dao.jdbc;

import com.edusmart.dao.GradeDao;
import com.edusmart.model.Grade;
import com.edusmart.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGradeDao implements GradeDao {

    private static final String TABLE = "grade";

    @Override
    public boolean create(Grade grade) {
        String sql = "INSERT INTO " + TABLE + " (student_id, course_id, subject, score, max_score, semester, academic_year, comment) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, grade);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        grade.setId(keys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create grade", ex);
        }
    }

    @Override
    public List<Grade> findAll() {
        String sql = "SELECT * FROM " + TABLE + " ORDER BY id";
        List<Grade> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch grades", ex);
        }
        return list;
    }

    @Override
    public Optional<Grade> findById(int id) {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch grade by id", ex);
        }
        return Optional.empty();
    }

    @Override
    public List<Grade> findByStudentId(int studentId) {
        String sql = "SELECT * FROM " + TABLE + " WHERE student_id = ? ORDER BY id";
        List<Grade> list = new ArrayList<>();
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch grades for student", ex);
        }
        return list;
    }

    @Override
    public boolean update(Grade grade) {
        String sql = "UPDATE " + TABLE + " SET student_id = ?, course_id = ?, subject = ?, score = ?, "
                + "max_score = ?, semester = ?, academic_year = ?, comment = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            fillStatement(ps, grade);
            ps.setInt(9, grade.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update grade", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete grade", ex);
        }
    }

    private void fillStatement(PreparedStatement ps, Grade g) throws SQLException {
        ps.setInt(1, g.getStudentId());
        ps.setInt(2, g.getCourseId());
        ps.setString(3, g.getSubject());
        ps.setDouble(4, g.getScore());
        ps.setDouble(5, g.getMaxScore());
        ps.setString(6, g.getSemester());
        ps.setString(7, g.getAcademicYear());
        ps.setString(8, g.getComment());
    }

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setId(rs.getInt("id"));
        g.setStudentId(rs.getInt("student_id"));
        g.setCourseId(rs.getInt("course_id"));
        g.setSubject(rs.getString("subject"));
        g.setScore(rs.getDouble("score"));
        g.setMaxScore(rs.getDouble("max_score"));
        g.setSemester(rs.getString("semester"));
        g.setAcademicYear(rs.getString("academic_year"));
        g.setComment(rs.getString("comment"));
        return g;
    }
}
