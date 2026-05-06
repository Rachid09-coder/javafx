-- Certification table (align with JdbcCertificationDao / Certification model).
-- If you already created the table without AUTO_INCREMENT / PRIMARY KEY, run the ALTER at the bottom.

CREATE TABLE IF NOT EXISTS `certification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(50) NOT NULL,
  `issued_at` datetime NOT NULL,
  `verification_code` varchar(30) NOT NULL,
  `pdf_path` varchar(255) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `unique_number` varchar(50) DEFAULT NULL,
  `valid_until` datetime DEFAULT NULL,
  `hmac_hash` varchar(255) DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `revocation_reason` longtext DEFAULT NULL,
  `student_id` int NOT NULL,
  `bulletin_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_certification_student` (`student_id`),
  KEY `idx_certification_bulletin` (`bulletin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Optional migration if your table matches the older DDL (id without AUTO_INCREMENT / no PK):
-- ALTER TABLE `certification` MODIFY COLUMN `id` int NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY (`id`);
