package com.edusmart.dao.jdbc;

import com.edusmart.dao.ExamQuestionDao;
import com.edusmart.model.ExamQuestion;
import com.edusmart.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of ExamQuestionDao.
 * Maps to the {@code exam_question} table.
 */
public class JdbcExamQuestionDao implements ExamQuestionDao {

    @Override
    public boolean create(ExamQuestion q) {
        String sql = "INSERT INTO exam_question (exam_id, question_text, question_type, correct_answer, max_points, order_index, options) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, q.getExamId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getQuestionType() != null ? q.getQuestionType().name() : null);
            ps.setString(4, q.getCorrectAnswer());
            ps.setDouble(5, q.getMaxPoints());
            ps.setInt(6, q.getOrderIndex());
            ps.setString(7, q.getOptions());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create exam question", ex);
        }
    }

    @Override
    public List<ExamQuestion> findByExamId(int examId) {
        String sql = "SELECT * FROM exam_question WHERE exam_id = ? ORDER BY order_index";
        List<ExamQuestion> list = new ArrayList<>();
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch questions for exam " + examId, ex);
        }
        return list;
    }

    @Override
    public Optional<ExamQuestion> findById(int id) {
        String sql = "SELECT * FROM exam_question WHERE id = ?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch question by id", ex);
        }
        return Optional.empty();
    }

    @Override
    public boolean update(ExamQuestion q) {
        String sql = "UPDATE exam_question SET question_text=?, question_type=?, correct_answer=?, " +
                     "max_points=?, order_index=?, options=? WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, q.getQuestionText());
            ps.setString(2, q.getQuestionType() != null ? q.getQuestionType().name() : null);
            ps.setString(3, q.getCorrectAnswer());
            ps.setDouble(4, q.getMaxPoints());
            ps.setInt(5, q.getOrderIndex());
            ps.setString(6, q.getOptions());
            ps.setInt(7, q.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update exam question", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM exam_question WHERE id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete exam question", ex);
        }
    }

    @Override
    public boolean deleteByExamId(int examId) {
        String sql = "DELETE FROM exam_question WHERE exam_id=?";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete questions for exam " + examId, ex);
        }
    }

    private ExamQuestion mapRow(ResultSet rs) throws SQLException {
        ExamQuestion q = new ExamQuestion();
        q.setId(rs.getInt("id"));
        q.setExamId(rs.getInt("exam_id"));
        q.setQuestionText(rs.getString("question_text"));
        String typeStr = rs.getString("question_type");
        if (typeStr != null) {
            try { q.setQuestionType(ExamQuestion.QuestionType.valueOf(typeStr)); }
            catch (IllegalArgumentException ignored) {}
        }
        q.setCorrectAnswer(rs.getString("correct_answer"));
        q.setMaxPoints(rs.getDouble("max_points"));
        q.setOrderIndex(rs.getInt("order_index"));
        q.setOptions(rs.getString("options"));
        return q;
    }
}
