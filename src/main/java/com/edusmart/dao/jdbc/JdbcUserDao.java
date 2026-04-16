package com.edusmart.dao.jdbc;

import com.edusmart.dao.UserDao;
import com.edusmart.model.User;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String TABLE = "`user`";

    @Override
    public boolean create(User user) {
        String sql = "INSERT INTO " + TABLE + " (name, prenom, email, role, password, numtel, is_active, "
                + "reset_token, reset_token_expires_at, google_id, face_descriptor, email_assoc) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillMutableColumns(ps, user);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create user", ex);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM " + TABLE + " ORDER BY id";
        List<User> list = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch users", ex);
        }
        return list;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM " + TABLE + " WHERE id = ?";

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
            throw new RuntimeException("Failed to fetch user by id", ex);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM " + TABLE + " WHERE email = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch user by email", ex);
        }
    }

    @Override
    public Optional<User> findByEmailAssoc(String emailAssoc) {
        String sql = "SELECT * FROM " + TABLE + " WHERE email_assoc = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, emailAssoc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch user by associated email", ex);
        }
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE " + TABLE + " SET name = ?, prenom = ?, email = ?, role = ?, password = ?, numtel = ?, "
                + "is_active = ?, reset_token = ?, reset_token_expires_at = ?, google_id = ?, face_descriptor = ?, email_assoc = ? "
                + "WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillMutableColumns(ps, user);
            ps.setInt(13, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update user", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM " + TABLE + " WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete user", ex);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setLastName(rs.getString("name"));
        u.setFirstName(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setRoleValue(rs.getString("role"));
        u.setPassword(rs.getString("password"));
        u.setNumtel(rs.getString("numtel"));
        u.setActive(rs.getInt("is_active") != 0);
        u.setResetToken(rs.getString("reset_token"));
        u.setResetTokenExpiresAt(toLocalDateTime(rs.getTimestamp("reset_token_expires_at")));
        u.setGoogleId(rs.getString("google_id"));
        u.setFaceDescriptor(rs.getString("face_descriptor"));
        u.setEmailAssoc(rs.getString("email_assoc"));
        return u;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private void fillMutableColumns(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getLastName());
        ps.setString(2, u.getFirstName());
        ps.setString(3, u.getEmail());
        ps.setString(4, u.getRoleValue());
        ps.setString(5, u.getPassword());
        ps.setString(6, u.getNumtel());
        ps.setInt(7, u.isActive() ? 1 : 0);
        ps.setString(8, u.getResetToken());
        if (u.getResetTokenExpiresAt() != null) {
            ps.setTimestamp(9, Timestamp.valueOf(u.getResetTokenExpiresAt()));
        } else {
            ps.setNull(9, Types.TIMESTAMP);
        }
        ps.setString(10, u.getGoogleId());
        if (u.getFaceDescriptor() != null) {
            ps.setString(11, u.getFaceDescriptor());
        } else {
            ps.setNull(11, Types.LONGVARCHAR);
        }
        ps.setString(12, u.getEmailAssoc());
    }
}
