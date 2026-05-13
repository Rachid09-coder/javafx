-- EduSmart Final Enhanced Test Data
-- Users: Teacher (ID=1), Student (ID=2)

-- 1. Categories
DELETE FROM category;
INSERT INTO category (id, name, description) VALUES
(1, 'Livres', 'Manuels scolaires et guides techniques'),
(2, 'Logiciels', 'Licences et outils de développement'),
(3, 'Matériel de cours', 'Cahiers, stylos et accessoires'),
(4, 'Équipement', 'Casques, clés USB et matériel informatique');

-- 2. Products for Shop
DELETE FROM product;
INSERT INTO product (name, description, price, stock, category_id, image) VALUES
('Livre Java Avancé', 'Maîtrisez JavaFX et JDBC avec ce guide complet.', 45.00, 15, 1, 'book_java.png'),
('Clé USB 64Go', 'Stockez vos projets EduSmart en toute sécurité.', 12.50, 50, 4, 'usb_drive.png'),
('Licence IntelliJ IDEA', 'Une licence d''un an pour le meilleur IDE.', 89.99, 10, 2, 'intellij.png'),
('Cahier de notes EduSmart', 'Élégant et pratique pour vos cours.', 5.50, 100, 3, 'notebook.png'),
('Casque Audio Bluetooth', 'Idéal pour suivre vos cours à distance.', 35.00, 20, 4, 'headphones.png'),
('Souris Ergonomique', 'Pour coder pendant des heures sans douleur.', 25.00, 30, 4, 'mouse.png');

-- 3. Exams
DELETE FROM exam;
INSERT INTO exam (id, title, description, type, duration, module_name, grade_category, academic_year, semester, coefficient, correction_published) VALUES
(1, 'Examen Final Algorithmique', 'Examen couvrant les tris et les graphes.', 'WRITTEN', 120, 'Informatique', 'EXAM', '2023-2024', 1, 3.0, 1),
(2, 'Quiz Bases de Données', 'Quiz rapide sur le langage SQL.', 'QUIZ', 30, 'Informatique', 'CC', '2023-2024', 1, 1.5, 0),
(3, 'Projet JavaFX', 'Développement d''une application de bureau.', 'PROJECT', 0, 'Informatique', 'TP', '2023-2024', 1, 2.0, 0);

-- 4. Exam Submissions
DELETE FROM exam_submission;
INSERT INTO exam_submission (student_id, exam_id, student_answer, score, ai_feedback, status) VALUES
(2, 1, 'Ma réponse à l''examen d''algorithmique... Les graphes sont complexes.', 16.5, 'Bonne compréhension des concepts de base.', 'GRADED'),
(2, 2, '1. SELECT, 2. INSERT, 3. UPDATE', 18.0, 'Excellent travail.', 'GRADED');

-- 5. Grades (Table 'grade')
DELETE FROM grade;
INSERT INTO grade (note, coefficient, session, academic_year, semester, student_id, course_id, created_at) VALUES
(16.5, 3.0, 'Janvier 2024', '2023-2024', '1', 2, 1, NOW()),
(18.0, 1.5, 'Novembre 2023', '2023-2024', '1', 2, 1, NOW()),
(14.0, 2.0, 'Décembre 2023', '2023-2024', '1', 2, 2, NOW());
