package com.edusmart.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Small DB migrator that ensures required shop tables exist at startup.
 * It is intentionally simple: uses IF NOT EXISTS and seeds a default row when empty.
 */
public final class DbMigrator {

    private DbMigrator() {}

    public static void ensureShopTablesExist() {
        try (Connection conn = DbConnection.getConnection(); Statement st = conn.createStatement()) {
            // Create category table
            String createCategory = "CREATE TABLE IF NOT EXISTS category ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "name VARCHAR(150) NOT NULL,"
                    + "description TEXT DEFAULT NULL,"
                    + "icon VARCHAR(255) DEFAULT NULL,"
                    + "color VARCHAR(50) DEFAULT NULL,"
                    + "PRIMARY KEY (id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            st.executeUpdate(createCategory);

            // Create product table
            String createProduct = "CREATE TABLE IF NOT EXISTS product ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + "name VARCHAR(255) NOT NULL,"
                    + "price DECIMAL(10,2) NOT NULL DEFAULT 0.00,"
                    + "stock INT NOT NULL DEFAULT 0,"
                    + "image VARCHAR(255) DEFAULT NULL,"
                    + "category_id INT DEFAULT NULL,"
                    + "PRIMARY KEY (id),"
                    + "KEY idx_product_category (category_id)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            st.executeUpdate(createProduct);

            // Seed default category if empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM category")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    st.executeUpdate("INSERT INTO category (name, description, icon, color) VALUES ('Default', 'Catégorie par défaut', NULL, NULL)");
                }
            }

            // Seed example product if empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM product")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // associate with first category if exists
                    try (ResultSet r2 = st.executeQuery("SELECT id FROM category LIMIT 1")) {
                        int catId = r2.next() ? r2.getInt(1) : 0;
                        if (catId > 0) {
                            st.executeUpdate("INSERT INTO product (name, price, stock, image, category_id) VALUES ('Example product', 9.99, 10, NULL, " + catId + ")");
                        } else {
                            st.executeUpdate("INSERT INTO product (name, price, stock, image) VALUES ('Example product', 9.99, 10, NULL)");
                        }
                    }
                }
            }
            
            // --- Exam & Submission Table Creation ---
            try {
                // Create exam table if missing
                String createExam = "CREATE TABLE IF NOT EXISTS exam ("
                        + "id INT NOT NULL AUTO_INCREMENT,"
                        + "title VARCHAR(255) NOT NULL,"
                        + "description TEXT,"
                        + "type VARCHAR(50),"
                        + "file_path VARCHAR(255),"
                        + "external_link VARCHAR(255),"
                        + "duration INT,"
                        + "module_name VARCHAR(100),"
                        + "grade_category VARCHAR(50),"
                        + "academic_year VARCHAR(50),"
                        + "semester INT,"
                        + "coefficient DOUBLE,"
                        + "course_id INT,"
                        + "correction_published TINYINT(1) DEFAULT 0,"
                        + "PRIMARY KEY (id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                st.executeUpdate(createExam);

                // Create exam_question table if missing
                String createExamQuestion = "CREATE TABLE IF NOT EXISTS exam_question ("
                        + "id INT NOT NULL AUTO_INCREMENT,"
                        + "exam_id INT NOT NULL,"
                        + "question_text TEXT NOT NULL,"
                        + "question_type VARCHAR(50) NOT NULL,"
                        + "correct_answer TEXT,"
                        + "max_points DOUBLE,"
                        + "order_index INT,"
                        + "options TEXT,"
                        + "PRIMARY KEY (id),"
                        + "KEY idx_question_exam (exam_id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                st.executeUpdate(createExamQuestion);

                // Create exam_submission table if missing
                String createSubmission = "CREATE TABLE IF NOT EXISTS exam_submission ("
                        + "id INT NOT NULL AUTO_INCREMENT,"
                        + "student_id INT NOT NULL,"
                        + "exam_id INT NOT NULL,"
                        + "question_id INT,"
                        + "student_answer TEXT,"
                        + "score DOUBLE,"
                        + "teacher_feedback TEXT,"
                        + "submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                        + "status VARCHAR(50),"
                        + "plagiarism_score DOUBLE,"
                        + "ai_feedback TEXT,"
                        + "PRIMARY KEY (id)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
                st.executeUpdate(createSubmission);
                
                System.out.println("DB migration: exam tables verified/created.");
            } catch (SQLException ex) {
                System.err.println("Migration error (Creation): " + ex.getMessage());
            }

            // --- Exam Table Migrations (Columns) ---
            try {
                st.executeUpdate("ALTER TABLE exam ADD COLUMN IF NOT EXISTS correction_published TINYINT(1) DEFAULT 0");
                st.executeUpdate("ALTER TABLE exam ADD COLUMN IF NOT EXISTS file_path VARCHAR(255) DEFAULT NULL");
                st.executeUpdate("ALTER TABLE exam ADD COLUMN IF NOT EXISTS external_link VARCHAR(255) DEFAULT NULL");
                st.executeUpdate("ALTER TABLE exam ADD COLUMN IF NOT EXISTS course_id INT DEFAULT NULL");
            } catch (SQLException ex) {
                if (!ex.getMessage().contains("Duplicate column")) {
                    System.err.println("Migration note (Exam): " + ex.getMessage());
                }
            }

            // --- Exam Submission Table Migrations (Columns) ---
            try {
                st.executeUpdate("ALTER TABLE exam_submission ADD COLUMN IF NOT EXISTS question_id INT DEFAULT NULL");
                st.executeUpdate("ALTER TABLE exam_submission ADD COLUMN IF NOT EXISTS submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                st.executeUpdate("ALTER TABLE exam_submission ADD COLUMN IF NOT EXISTS plagiarism_score DOUBLE DEFAULT NULL");
                st.executeUpdate("ALTER TABLE exam_submission ADD COLUMN IF NOT EXISTS ai_feedback TEXT DEFAULT NULL");
            } catch (SQLException ex) {
                if (!ex.getMessage().contains("Duplicate column")) {
                    System.err.println("Migration note (Submission): " + ex.getMessage());
                }
            }

            System.out.println("DB migration: all core tables ensured.");
        } catch (SQLException ex) {
            System.err.println("DB migration failed (safe to ignore if DB unavailable): " + ex.getMessage());
        }
    }
}
