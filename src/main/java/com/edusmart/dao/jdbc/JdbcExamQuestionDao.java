package com.edusmart.dao.jdbc;

import com.edusmart.dao.ExamQuestionDao;
import com.edusmart.model.ExamQuestion;
import com.edusmart.util.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcExamQuestionDao implements ExamQuestionDao {

    @Override
    public List<ExamQuestion> findByExamId(int examId) {
        List<ExamQuestion> questions = new ArrayList<>();
        String sql = "SELECT * FROM exam_question WHERE exam_id = ? ORDER BY order_index";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                questions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    @Override
    public ExamQuestion findById(int id) {
        String sql = "SELECT * FROM exam_question WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean create(ExamQuestion q) {
        String sql = "INSERT INTO exam_question (exam_id, question_text, question_type, correct_answer, max_points, order_index, options) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, q.getExamId());
            ps.setString(2, q.getQuestionText());
            ps.setString(3, q.getQuestionType() != null ? q.getQuestionType().name() : ExamQuestion.QuestionType.OPEN_ENDED.name());
            ps.setString(4, q.getCorrectAnswer());
            ps.setDouble(5, q.getMaxPoints());
            ps.setInt(6, q.getOrderIndex());
            ps.setString(7, q.getOptions());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(ExamQuestion q) {
        String sql = "UPDATE exam_question SET question_text = ?, question_type = ?, correct_answer = ?, max_points = ?, order_index = ?, options = ? WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getQuestionText());
            ps.setString(2, q.getQuestionType().name());
            ps.setString(3, q.getCorrectAnswer());
            ps.setDouble(4, q.getMaxPoints());
            ps.setInt(5, q.getOrderIndex());
            ps.setString(6, q.getOptions());
            ps.setInt(7, q.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM exam_question WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ExamQuestion mapRow(ResultSet rs) throws SQLException {
        ExamQuestion q = new ExamQuestion();
        q.setId(rs.getInt("id"));
        q.setExamId(rs.getInt("exam_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setQuestionType(ExamQuestion.QuestionType.fromString(rs.getString("question_type")));
        q.setCorrectAnswer(rs.getString("correct_answer"));
        q.setMaxPoints(rs.getDouble("max_points"));
        q.setOrderIndex(rs.getInt("order_index"));
        q.setOptions(rs.getString("options"));
        return q;
    }
}
