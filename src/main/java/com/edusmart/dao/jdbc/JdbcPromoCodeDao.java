package com.edusmart.dao.jdbc;

import com.edusmart.dao.PromoCodeDao;
import com.edusmart.model.PromoCode;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class JdbcPromoCodeDao implements PromoCodeDao {

    @Override
    public boolean create(PromoCode promoCode) {
        if (promoCode == null) throw new IllegalArgumentException("promoCode is required");
        String sql = "INSERT INTO promo_code (code, discount_percent, active) VALUES (?, ?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, promoCode.getCode() != null ? promoCode.getCode().trim() : null);
            ps.setDouble(2, promoCode.getDiscountPercent());
            ps.setBoolean(3, promoCode.isActive());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create promo code", ex);
        }
    }

    @Override
    public Optional<PromoCode> findActiveByCode(String code) {
        if (code == null || code.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM promo_code WHERE code = ? AND active = TRUE LIMIT 1";

        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch promo code", ex);
        }
    }

    @Override
    public boolean hasStudentUsedPromo(int promoCodeId, int studentId) {
        String sql = "SELECT 1 FROM promo_code_usage WHERE promo_code_id = ? AND student_id = ? LIMIT 1";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, promoCodeId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to check promo usage", ex);
        }
    }

    @Override
    public boolean markUsed(int promoCodeId, int studentId) {
        String sql = "INSERT INTO promo_code_usage (promo_code_id, student_id) VALUES (?, ?)";
        try (Connection c = DbConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, promoCodeId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            // duplicate (already used) -> return false
            String state = ex.getSQLState();
            if (state != null && state.startsWith("23")) return false;
            throw new RuntimeException("Failed to mark promo usage", ex);
        }
    }

    private PromoCode mapRow(ResultSet rs) throws SQLException {
        PromoCode p = new PromoCode();
        p.setId(rs.getInt("id"));
        p.setCode(rs.getString("code"));
        p.setDiscountPercent(rs.getDouble("discount_percent"));
        p.setActive(rs.getBoolean("active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) p.setCreatedAt(ts.toLocalDateTime());
        return p;
    }
}

