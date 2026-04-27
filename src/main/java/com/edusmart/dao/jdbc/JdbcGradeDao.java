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
        String sql = "INSERT INTO " + TABLE + " (note, coefficient, session, academic_year, semester, created_at, student_id, module_id, course_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        String sql = "UPDATE " + TABLE + " SET note = ?, coefficient = ?, session = ?, academic_year = ?, "
                + "semester = ?, student_id = ?, module_id = ?, course_id = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, grade.getNote());
            ps.setDouble(2, grade.getCoefficient() != null ? grade.getCoefficient() : 1.0);
            ps.setString(3, grade.getSession() != null ? grade.getSession() : "Principale");
            ps.setString(4, grade.getAcademicYear());
            ps.setString(5, grade.getSemester());
            ps.setInt(6, grade.getStudentId());
            ps.setObject(7, grade.getModuleId() != null ? grade.getModuleId() : 0);
            ps.setInt(8, grade.getCourseId());
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
        ps.setDouble(1, g.getNote());
        ps.setDouble(2, g.getCoefficient() != null ? g.getCoefficient() : 1.0);
        ps.setString(3, g.getSession() != null ? g.getSession() : "Principale");
        ps.setString(4, g.getAcademicYear());
        ps.setString(5, g.getSemester());
        ps.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
        ps.setInt(7, g.getStudentId());
        ps.setObject(8, g.getModuleId() != null ? g.getModuleId() : 0); // fallback to 0 to avoid field error
        ps.setInt(9, g.getCourseId());
    }

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade g = new Grade();
        g.setId(rs.getInt("id"));
        g.setNote(rs.getDouble("note"));
        g.setCoefficient(rs.getDouble("coefficient"));
        g.setSession(rs.getString("session"));
        g.setAcademicYear(rs.getString("academic_year"));
        g.setSemester(rs.getString("semester"));
        g.setStudentId(rs.getInt("student_id"));
        g.setModuleId(rs.getObject("module_id") != null ? rs.getInt("module_id") : null);
        g.setCourseId(rs.getInt("course_id"));
        return g;
    }
}
