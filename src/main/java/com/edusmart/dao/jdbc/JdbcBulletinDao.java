package com.edusmart.dao.jdbc;

import com.edusmart.dao.BulletinDao;
import com.edusmart.model.Bulletin;
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

public class JdbcBulletinDao implements BulletinDao {

    @Override
    public boolean create(Bulletin bulletin) {
        String sql = "INSERT INTO bulletin (academic_year, semester, average, status, mention, class_rank, hmac_hash, "
                + "pdf_path, verification_code, validated_at, published_at, revoked_at, revocation_reason, "
                + "created_at, updated_at, student_id, validated_by_id, published_by_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, bulletin);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to create bulletin", ex);
        }
    }

    @Override
    public List<Bulletin> findAll() {
        String sql = "SELECT * FROM bulletin ORDER BY id";
        List<Bulletin> list = new ArrayList<>();

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to fetch bulletins", ex);
        }
        return list;
    }

    @Override
    public Optional<Bulletin> findById(int id) {
        String sql = "SELECT * FROM bulletin WHERE id = ?";

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
            throw new RuntimeException("Failed to fetch bulletin by id", ex);
        }
    }

    @Override
    public boolean update(Bulletin bulletin) {
        String sql = "UPDATE bulletin SET academic_year = ?, semester = ?, average = ?, status = ?, mention = ?, "
                + "class_rank = ?, hmac_hash = ?, pdf_path = ?, verification_code = ?, validated_at = ?, "
                + "published_at = ?, revoked_at = ?, revocation_reason = ?, created_at = ?, updated_at = ?, "
                + "student_id = ?, validated_by_id = ?, published_by_id = ? WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, bulletin);
            ps.setInt(19, bulletin.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update bulletin", ex);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM bulletin WHERE id = ?";

        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete bulletin", ex);
        }
    }

    @Override
    public int findRankByAverage(String semester, double average) {
        String sql = "SELECT COUNT(*) + 1 FROM bulletin WHERE semester = ? AND average > ?";
        try (Connection connection = DbConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, semester);
            ps.setDouble(2, average);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to calculate rank", ex);
        }
        return 1;
    }

    private Bulletin mapRow(ResultSet rs) throws SQLException {
        Bulletin b = new Bulletin();
        b.setId(rs.getInt("id"));
        b.setAcademicYear(rs.getString("academic_year"));
        b.setSemester(rs.getString("semester"));
        double avg = rs.getDouble("average");
        b.setAverage(rs.wasNull() ? null : avg);
        b.setStatus(rs.getString("status"));
        b.setMention(rs.getString("mention"));
        int rank = rs.getInt("class_rank");
        b.setClassRank(rs.wasNull() ? null : rank);
        b.setHmacHash(rs.getString("hmac_hash"));
        b.setPdfPath(rs.getString("pdf_path"));
        b.setVerificationCode(rs.getString("verification_code"));
        b.setValidatedAt(toLocalDateTime(rs.getTimestamp("validated_at")));
        b.setPublishedAt(toLocalDateTime(rs.getTimestamp("published_at")));
        b.setRevokedAt(toLocalDateTime(rs.getTimestamp("revoked_at")));
        b.setRevocationReason(rs.getString("revocation_reason"));
        b.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        b.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
        b.setStudentId(rs.getInt("student_id"));
        int vBy = rs.getInt("validated_by_id");
        b.setValidatedById(rs.wasNull() ? null : vBy);
        int pBy = rs.getInt("published_by_id");
        b.setPublishedById(rs.wasNull() ? null : pBy);
        return b;
    }

    private static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private void fillStatement(PreparedStatement ps, Bulletin b) throws SQLException {
        ps.setString(1, b.getAcademicYear());
        ps.setString(2, b.getSemester());
        if (b.getAverage() != null) {
            ps.setDouble(3, b.getAverage());
        } else {
            ps.setNull(3, Types.DOUBLE);
        }
        ps.setString(4, b.getStatus());
        ps.setString(5, b.getMention());
        if (b.getClassRank() != null) {
            ps.setInt(6, b.getClassRank());
        } else {
            ps.setNull(6, Types.INTEGER);
        }
        ps.setString(7, b.getHmacHash());
        ps.setString(8, b.getPdfPath());
        ps.setString(9, b.getVerificationCode());
        setTimestamp(ps, 10, b.getValidatedAt());
        setTimestamp(ps, 11, b.getPublishedAt());
        setTimestamp(ps, 12, b.getRevokedAt());
        ps.setString(13, b.getRevocationReason());
        if (b.getCreatedAt() != null) {
            ps.setTimestamp(14, Timestamp.valueOf(b.getCreatedAt()));
        } else {
            ps.setNull(14, Types.TIMESTAMP);
        }
        setTimestamp(ps, 15, b.getUpdatedAt());
        ps.setInt(16, b.getStudentId());
        if (b.getValidatedById() != null) {
            ps.setInt(17, b.getValidatedById());
        } else {
            ps.setNull(17, Types.INTEGER);
        }
        if (b.getPublishedById() != null) {
            ps.setInt(18, b.getPublishedById());
        } else {
            ps.setNull(18, Types.INTEGER);
        }
    }

    private static void setTimestamp(PreparedStatement ps, int idx, LocalDateTime dt) throws SQLException {
        if (dt != null) {
            ps.setTimestamp(idx, Timestamp.valueOf(dt));
        } else {
            ps.setNull(idx, Types.TIMESTAMP);
        }
    }
}
