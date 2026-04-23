package com.edusmart.dao.jdbc;

import com.edusmart.dao.CertificationDao;
import com.edusmart.model.Certification;
import com.edusmart.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCertificationDao implements CertificationDao {

    private static final String TABLE = "`certification`";

    private static final String SELECT_BASE = "SELECT id, `type`, issued_at, verification_code, pdf_path, status, "
            + "unique_number, valid_until, hmac_hash, revoked_at, revocation_reason, student_id, bulletin_id, metier FROM "
            + TABLE + " ";

    @Override
    public List<Certification> findAll() {
        String sql = SELECT_BASE + "ORDER BY issued_at DESC, id DESC";
        List<Certification> list = new ArrayList<>();
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch certifications", ex);
        }
        return list;
    }

    @Override
    public List<Certification> findByStudentId(int studentId) {
        String sql = SELECT_BASE + "WHERE student_id = ? ORDER BY issued_at DESC, id DESC";
        List<Certification> list = new ArrayList<>();
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch certifications for student", ex);
        }
        return list;
    }

    @Override
    public Optional<Certification> findById(int id) {
        String sql = SELECT_BASE + "WHERE id = ?";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch certification by id", ex);
        }
        return Optional.empty();
    }

    @Override
    public boolean create(Certification c) {
        String sql = "INSERT INTO " + TABLE + " (`type`, issued_at, verification_code, pdf_path, status, unique_number, "
                + "valid_until, hmac_hash, revoked_at, revocation_reason, student_id, bulletin_id, metier) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillInsert(ps, c);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                return false;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
            return true;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create certification", ex);
        }
    }

    @Override
    public boolean update(Certification c) {
        String sql = "UPDATE " + TABLE + " SET `type` = ?, issued_at = ?, verification_code = ?, pdf_path = ?, "
                + "status = ?, unique_number = ?, valid_until = ?, hmac_hash = ?, revoked_at = ?, "
                + "revocation_reason = ?, student_id = ?, bulletin_id = ?, metier = ? WHERE id = ?";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillInsert(ps, c);
            ps.setInt(14, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update certification", ex);
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
            throw new RuntimeException("Failed to delete certification", ex);
        }
    }

    @Override
    public boolean updateRevocation(int id, String status, LocalDateTime revokedAt, String revocationReason) {
        String sql = "UPDATE " + TABLE + " SET status = ?, revoked_at = ?, revocation_reason = ? WHERE id = ?";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, revokedAt != null ? Timestamp.valueOf(revokedAt) : null);
            ps.setString(3, revocationReason);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to revoke certification", ex);
        }
    }

    private static void fillInsert(PreparedStatement ps, Certification c) throws SQLException {
        ps.setString(1, c.getCertificationType());
        ps.setTimestamp(2, c.getIssuedAt() != null ? Timestamp.valueOf(c.getIssuedAt()) : null);
        ps.setString(3, c.getVerificationCode());
        if (c.getPdfPath() != null) {
            ps.setString(4, c.getPdfPath());
        } else {
            ps.setNull(4, Types.VARCHAR);
        }
        ps.setString(5, c.getStatus());
        if (c.getUniqueNumber() != null) {
            ps.setString(6, c.getUniqueNumber());
        } else {
            ps.setNull(6, Types.VARCHAR);
        }
        if (c.getValidUntil() != null) {
            ps.setTimestamp(7, Timestamp.valueOf(c.getValidUntil()));
        } else {
            ps.setNull(7, Types.TIMESTAMP);
        }
        if (c.getHmacHash() != null) {
            ps.setString(8, c.getHmacHash());
        } else {
            ps.setNull(8, Types.VARCHAR);
        }
        if (c.getRevokedAt() != null) {
            ps.setTimestamp(9, Timestamp.valueOf(c.getRevokedAt()));
        } else {
            ps.setNull(9, Types.TIMESTAMP);
        }
        if (c.getRevocationReason() != null) {
            ps.setString(10, c.getRevocationReason());
        } else {
            ps.setNull(10, Types.LONGVARCHAR);
        }
        ps.setInt(11, c.getStudentId());
        if (c.getBulletinId() != null) {
            ps.setInt(12, c.getBulletinId());
        } else {
            ps.setNull(12, Types.INTEGER);
        }
        ps.setString(13, c.getMetier());
    }

    private static Certification mapRow(ResultSet rs) throws SQLException {
        Certification c = new Certification();
        c.setId(rs.getInt("id"));
        c.setCertificationType(rs.getString("type"));
        Timestamp issued = rs.getTimestamp("issued_at");
        c.setIssuedAt(issued != null ? issued.toLocalDateTime() : null);
        c.setVerificationCode(rs.getString("verification_code"));
        c.setPdfPath(rs.getString("pdf_path"));
        c.setStatus(rs.getString("status"));
        c.setUniqueNumber(rs.getString("unique_number"));
        Timestamp validUntil = rs.getTimestamp("valid_until");
        c.setValidUntil(validUntil != null ? validUntil.toLocalDateTime() : null);
        c.setHmacHash(rs.getString("hmac_hash"));
        Timestamp revoked = rs.getTimestamp("revoked_at");
        c.setRevokedAt(revoked != null ? revoked.toLocalDateTime() : null);
        c.setRevocationReason(rs.getString("revocation_reason"));
        c.setStudentId(rs.getInt("student_id"));
        int bid = rs.getInt("bulletin_id");
        if (rs.wasNull()) {
            c.setBulletinId(null);
        } else {
            c.setBulletinId(bid);
        }
        c.setMetier(rs.getString("metier"));
        return c;
    }
}
