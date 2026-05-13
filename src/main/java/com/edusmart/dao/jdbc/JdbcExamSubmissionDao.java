package com.edusmart.dao.jdbc;

import com.edusmart.dao.ExamSubmissionDao;
import com.edusmart.model.ExamSubmission;
import com.edusmart.util.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExamSubmissionDao implements ExamSubmissionDao {

    @Override
    public List<ExamSubmission> findByExamAndQuestion(int examId, int questionId) {
        List<ExamSubmission> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_submission WHERE exam_id = ? AND question_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.setInt(2, questionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ExamSubmission> findByExam(int examId) {
        List<ExamSubmission> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_submission WHERE exam_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ExamSubmission findById(int id) {
        String sql = "SELECT * FROM exam_submission WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ExamSubmission findByStudentAndExam(int studentId, int examId) {
        String sql = "SELECT * FROM exam_submission WHERE student_id = ? AND exam_id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean update(ExamSubmission s) {
        String sql = "UPDATE exam_submission SET score = ?, teacher_feedback = ?, status = ?, plagiarism_score = ?, ai_feedback = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (s.getScore() != null) ps.setDouble(1, s.getScore()); else ps.setNull(1, Types.DOUBLE);
            ps.setString(2, s.getTeacherFeedback());
            ps.setString(3, s.getStatus().name());
            if (s.getPlagiarismScore() != null) ps.setDouble(4, s.getPlagiarismScore()); else ps.setNull(4, Types.DOUBLE);
            ps.setString(5, s.getAiFeedback());
            ps.setInt(6, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean create(ExamSubmission s) {
        String sql = "INSERT INTO exam_submission (student_id, exam_id, question_id, student_answer, submission_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getStudentId());
            ps.setInt(2, s.getExamId());
            ps.setInt(3, s.getQuestionId());
            ps.setString(4, s.getStudentAnswer());
            ps.setTimestamp(5, Timestamp.valueOf(s.getSubmissionDate()));
            ps.setString(6, s.getStatus().name());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamSubmission mapRow(ResultSet rs) throws SQLException {
        ExamSubmission s = new ExamSubmission();
        s.setId(rs.getInt("id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setExamId(rs.getInt("exam_id"));
        s.setQuestionId(rs.getInt("question_id"));
        s.setStudentAnswer(rs.getString("student_answer"));
        s.setScore(rs.getObject("score", Double.class));
        s.setTeacherFeedback(rs.getString("teacher_feedback"));
        Timestamp ts = rs.getTimestamp("submission_date");
        if (ts != null) {
            s.setSubmissionDate(ts.toLocalDateTime());
        } else {
            s.setSubmissionDate(java.time.LocalDateTime.now());
        }
        s.setStatus(ExamSubmission.Status.valueOf(rs.getString("status")));
        s.setPlagiarismScore(rs.getObject("plagiarism_score", Double.class));
        s.setAiFeedback(rs.getString("ai_feedback"));
        return s;
    }
}
