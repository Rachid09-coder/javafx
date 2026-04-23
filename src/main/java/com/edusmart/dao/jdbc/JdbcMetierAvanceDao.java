package com.edusmart.dao.jdbc;

import com.edusmart.model.MetierAvance;
import com.edusmart.util.DbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcMetierAvanceDao {

    public List<MetierAvance> findAll() {
        List<MetierAvance> list = new ArrayList<>();
        String sql = "SELECT * FROM metier_avance";
        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new MetierAvance(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("description"),
                    rs.getInt("metier_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean create(MetierAvance m) {
        String sql = "INSERT INTO metier_avance (nom, description, metier_id) VALUES (?, ?, ?)";
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getNom());
            ps.setString(2, m.getDescription());
            ps.setInt(3, m.getMetierId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) m.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
