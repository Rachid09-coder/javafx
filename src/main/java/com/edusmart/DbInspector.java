package com.edusmart;

import com.edusmart.util.DbConnection;
import java.sql.*;

public class DbInspector {
    public static void main(String[] args) {
        try (Connection conn = DbConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("--- TABLES ---");
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                System.out.println("Table: " + tables.getString("TABLE_NAME"));
            }
            
            System.out.println("\n--- EXAM COLUMNS ---");
            ResultSet columns = metaData.getColumns(null, null, "exam", null);
            while (columns.next()) {
                System.out.println("Column: " + columns.getString("COLUMN_NAME"));
            }
            
            System.out.println("\n--- FOREIGN KEYS REFERENCING EXAM ---");
            ResultSet fks = metaData.getExportedKeys(null, null, "exam");
            while (fks.next()) {
                System.out.println("FK from Table: " + fks.getString("FKTABLE_NAME") + " (Column: " + fks.getString("FKCOLUMN_NAME") + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
