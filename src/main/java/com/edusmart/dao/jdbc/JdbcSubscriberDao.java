package com.edusmart.dao.jdbc;

import com.edusmart.dao.SubscriberDao;
import com.edusmart.model.Subscriber;
import com.edusmart.util.DbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcSubscriberDao implements SubscriberDao {

    public JdbcSubscriberDao() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS subscribers (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "email VARCHAR(255) UNIQUE NOT NULL, " +
                     "subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addSubscriber(Subscriber subscriber) {
        String sql = "INSERT INTO subscribers (email) VALUES (?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subscriber.getEmail());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeSubscriber(String email) {
        String sql = "DELETE FROM subscribers WHERE email = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Subscriber> getAllSubscribers() {
        List<Subscriber> list = new ArrayList<>();
        String sql = "SELECT * FROM subscribers";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Subscriber sub = new Subscriber();
                sub.setId(rs.getInt("id"));
                sub.setEmail(rs.getString("email"));
                Timestamp ts = rs.getTimestamp("subscribed_at");
                if (ts != null) {
                    sub.setSubscribedAt(ts.toLocalDateTime());
                }
                list.add(sub);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean exists(String email) {
        String sql = "SELECT COUNT(*) FROM subscribers WHERE email = ?";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
