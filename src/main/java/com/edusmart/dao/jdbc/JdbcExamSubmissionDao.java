package com.edusmart.dao.jdbc;

import com.edusmart.dao.ExamSubmissionDao;
import com.edusmart.model.ExamSubmission;
import com.edusmart.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ExamSubmissionDao.
 * Maps to the {@code exam_submission} table, joining {@code user} for student details.
 */
public class JdbcExamSubmissionDao implements ExamSubmissionDao {

    @Override
    public boolean create(ExamSubmission s) {
        // Debug parameters
        System.out.println("studentId=" + s.getStudentId());
        System.out.println("examId=" + s.getExamId());
        System.out.println("filePath=" + s.getFilePath());

        String sql = "INSERT INTO exam_submission (student_id, exam_id, file_path, submitted_at) " +
                     "VALUES (?, ?, ?, NOW())";
        
        System.out.println("Executing Query: " + sql);
        
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, s.getStudentId());
            ps.setInt(2, s.getExamId());
            ps.setString(3, s.getFilePath());
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) s.setId(keys.getInt(1));
                }
            }
            return rows > 0;
        } catch (SQLException ex) {
            System.err.println("[Database Error] Insertion failed: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("Failed to create exam submission: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<ExamSubmission> findByExamId(int examId) {
        String sql = buildSelectSql("s.exam_id = ?");
        return query(sql, ps -> ps.setInt(1, examId));
    }

    @Override
    public List<com.edusmart.model.StudentSubmission> getStudentSubmissions() {
        String sql = "SELECT u.id, u.name, u.prenom, u.email, u.role, s.file_path " +
                     "FROM user u " +
                     "LEFT JOIN exam_submission s ON u.id = s.student_id " +
                     "WHERE LOWER(u.role) IN ('etudiant', 'student') " +
                     "ORDER BY u.name, u.prenom";
        List<com.edusmart.model.StudentSubmission> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new com.edusmart.model.StudentSubmission(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("file_path")
                    ));
                }
            }
            System.out.println("Loaded students: " + list.size());
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch student submissions", ex);
        }
        return list;
    }

    @Override
    public List<com.edusmart.model.StudentSubmission> getStudentSubmissions(int examId) {
        String sql = "SELECT u.id, u.name, u.prenom, u.email, u.role, s.file_path " +
                     "FROM user u " +
                     "LEFT JOIN exam_submission s ON u.id = s.student_id AND s.exam_id = ? " +
                     "WHERE LOWER(u.role) IN ('etudiant', 'student') " +
                     "ORDER BY u.name, u.prenom";
        List<com.edusmart.model.StudentSubmission> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new com.edusmart.model.StudentSubmission(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("file_path")
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch student submissions for exam: " + examId, ex);
        }
        return list;
    }

    @Override
    public List<ExamSubmission> findByStudentId(int studentId) {
        String sql = buildSelectSql("s.student_id = ?");
        return query(sql, ps -> ps.setInt(1, studentId));
    }

    @Override
    public List<ExamSubmission> findByExamAndStudent(int examId, int studentId) {
        String sql = buildSelectSql("s.exam_id = ? AND s.student_id = ?");
        return query(sql, ps -> { ps.setInt(1, examId); ps.setInt(2, studentId); });
    }

    @Override
    public List<ExamSubmission> findByQuestionId(int questionId) {
        String sql = buildSelectSql("s.question_id = ?");
        return query(sql, ps -> ps.setInt(1, questionId));
    }

    @Override
    public Optional<ExamSubmission> findById(int id) {
        String sql = buildSelectSql("s.id = ?");
        List<ExamSubmission> results = query(sql, ps -> ps.setInt(1, id));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean update(ExamSubmission s) {
        String sql = "UPDATE exam_submission SET student_answer=?, score=?, max_score=?, " +
                     "ai_feedback=?, ai_confidence=?, plagiarism_score=?, status=?, graded_at=?, file_path=? " +
                     "WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getStudentAnswer());
            ps.setDouble(2, s.getScore());
            ps.setDouble(3, s.getMaxScore());
            ps.setString(4, s.getAiFeedback());
            ps.setDouble(5, s.getAiConfidence());
            ps.setDouble(6, s.getPlagiarismScore());
            ps.setString(7, s.getStatus() != null ? s.getStatus().name() : null);
            ps.setTimestamp(8, s.getGradedAt() != null ? Timestamp.valueOf(s.getGradedAt()) : null);
            ps.setInt(9, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update exam submission", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM exam_submission WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete exam submission", ex);
        }
    }

    // ── Private helpers ────────────────────────────────────────────────

    private String buildSelectSql(String whereClause) {
        return "SELECT s.*, u.prenom AS student_first, u.name AS student_last, u.email AS student_email " +
               "FROM exam_submission s " +
               "JOIN user u ON s.student_id = u.id " +
               "WHERE LOWER(u.role) IN ('etudiant', 'student') AND " + whereClause + " ORDER BY s.student_id";
    }

    @FunctionalInterface
    private interface StatementFiller {
        void fill(PreparedStatement ps) throws SQLException;
    }

    private List<ExamSubmission> query(String sql, StatementFiller filler) {
        List<ExamSubmission> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            filler.fill(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to query exam submissions", ex);
        }
        return list;
    }

    private void fillStatement(PreparedStatement ps, ExamSubmission s) throws SQLException {
        ps.setInt(1, s.getStudentId());
        ps.setInt(2, s.getExamId());
        ps.setString(3, s.getFilePath());
        ps.setString(4, s.getStatus() != null ? s.getStatus().name() : "SUBMITTED");
    }

    private ExamSubmission mapRow(ResultSet rs) throws SQLException {
        ExamSubmission s = new ExamSubmission();
        s.setId(rs.getInt("id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setExamId(rs.getInt("exam_id"));
        s.setFilePath(rs.getString("file_path"));
        
        Timestamp ts = rs.getTimestamp("submitted_at");
        if (ts != null) s.setSubmittedAt(ts.toLocalDateTime());
        
        return s;
    }
}
