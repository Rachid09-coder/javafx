-- Create tables required by the Shop feature (category + product)
-- Run this script on the same database configured in `DbConnection` (default: db_recovery_final).

CREATE TABLE IF NOT EXISTS `category` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `icon` VARCHAR(255) DEFAULT NULL,
  `color` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS `product` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `stock` INT NOT NULL DEFAULT 0,
  `image` VARCHAR(255) DEFAULT NULL,
  `category_id` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_product_category` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Optional: seed example data
INSERT INTO `category` (`name`, `description`, `icon`, `color`) 
  SELECT 'Default', 'Catégorie par défaut', NULL, NULL
  WHERE NOT EXISTS (SELECT 1 FROM `category` LIMIT 1);

INSERT INTO `product` (`name`, `price`, `stock`, `image`, `category_id`)
  SELECT 'Example product', 9.99, 10, NULL, (SELECT id FROM category LIMIT 1)
  WHERE NOT EXISTS (SELECT 1 FROM `product` LIMIT 1);
