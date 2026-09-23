-- =======================================================
-- DUET MEAL MANAGEMENT SYSTEM — MYSQL DATABASE DUMP
-- Import this file into phpMyAdmin or MySQL
-- =======================================================

USE `if0_42848152_duetmeal`;

-- --------------------------------------------------------
-- Table: users
-- --------------------------------------------------------
DROP TABLE IF EXISTS `notice_reads`;
DROP TABLE IF EXISTS `transactions`;
DROP TABLE IF EXISTS `meal_history`;
DROP TABLE IF EXISTS `bookings`;
DROP TABLE IF EXISTS `recharge_requests`;
DROP TABLE IF EXISTS `wallets`;
DROP TABLE IF EXISTS `daily_menus`;
DROP TABLE IF EXISTS `meal_rates`;
DROP TABLE IF EXISTS `notices`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `full_name` VARCHAR(191) NOT NULL,
  `email` VARCHAR(191) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(64) DEFAULT NULL,
  `initials` VARCHAR(10) DEFAULT NULL,
  `profile_photo` VARCHAR(255) DEFAULT NULL,
  `resident_type` VARCHAR(20) DEFAULT 'outside',
  `user_type` VARCHAR(20) DEFAULT 'teacher',
  `meal_reminder` TINYINT(1) DEFAULT 1,
  `auto_booking` TINYINT(1) DEFAULT 0,
  `auth_token` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: wallets
-- --------------------------------------------------------
CREATE TABLE `wallets` (
  `user_id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `total_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `available_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `currency` VARCHAR(10) DEFAULT 'BDT',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: recharge_requests
-- --------------------------------------------------------
CREATE TABLE `recharge_requests` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `user_id` VARCHAR(64) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'pending',
  `admin_note` TEXT DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `approved_at` DATETIME DEFAULT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: bookings
-- --------------------------------------------------------
CREATE TABLE `bookings` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `user_id` VARCHAR(64) NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `include_lunch` TINYINT(1) DEFAULT 0,
  `include_dinner` TINYINT(1) DEFAULT 0,
  `guest_count` INT NOT NULL DEFAULT 0,
  `total_people` INT NOT NULL DEFAULT 1,
  `price_per_meal` DECIMAL(10,2) NOT NULL DEFAULT 90.00,
  `total_cost` DECIMAL(10,2) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'active',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: meal_history
-- --------------------------------------------------------
CREATE TABLE `meal_history` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `user_id` VARCHAR(64) NOT NULL,
  `booking_id` VARCHAR(64) DEFAULT NULL,
  `date` DATE NOT NULL,
  `meal_type` VARCHAR(20) NOT NULL,
  `guest_count` INT NOT NULL DEFAULT 0,
  `participants` VARCHAR(100) NOT NULL,
  `status` VARCHAR(20) DEFAULT 'booked',
  `price` DECIMAL(10,2) NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: transactions
-- --------------------------------------------------------
CREATE TABLE `transactions` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `user_id` VARCHAR(64) NOT NULL,
  `type` VARCHAR(30) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `sign` VARCHAR(20) NOT NULL,
  `balance_after` DECIMAL(10,2) NOT NULL,
  `reference_id` VARCHAR(64) DEFAULT NULL,
  `timestamp` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: notices
-- --------------------------------------------------------
CREATE TABLE `notices` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `category` VARCHAR(50) NOT NULL,
  `tag` VARCHAR(50) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT NOT NULL,
  `author_id` VARCHAR(64) DEFAULT 'admin',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: notice_reads
-- --------------------------------------------------------
CREATE TABLE `notice_reads` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `notice_id` VARCHAR(64) NOT NULL,
  `user_id` VARCHAR(64) NOT NULL,
  `read_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `unique_user_notice` (`notice_id`, `user_id`),
  FOREIGN KEY (`notice_id`) REFERENCES `notices`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: daily_menus
-- --------------------------------------------------------
CREATE TABLE `daily_menus` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `date` DATE NOT NULL UNIQUE,
  `lunch_venue` VARCHAR(100) DEFAULT 'Main Canteen',
  `lunch_start` VARCHAR(10) DEFAULT '12:30',
  `lunch_end` VARCHAR(10) DEFAULT '14:00',
  `lunch_cutoff` VARCHAR(10) DEFAULT '12:00',
  `lunch_items` TEXT DEFAULT NULL,
  `lunch_max_guests` INT DEFAULT 10,
  `dinner_venue` VARCHAR(100) DEFAULT 'Main Canteen',
  `dinner_start` VARCHAR(10) DEFAULT '19:30',
  `dinner_end` VARCHAR(10) DEFAULT '21:00',
  `dinner_cutoff_advance_hours` INT DEFAULT 24,
  `dinner_items` TEXT DEFAULT NULL,
  `dinner_max_guests` INT DEFAULT 10
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table: meal_rates
-- --------------------------------------------------------
CREATE TABLE `meal_rates` (
  `id` VARCHAR(64) NOT NULL PRIMARY KEY,
  `month` VARCHAR(7) NOT NULL UNIQUE,
  `rate` DECIMAL(10,2) NOT NULL,
  `is_finalized` TINYINT(1) DEFAULT 0,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =======================================================
-- INITIAL SEED DATA
-- =======================================================

-- Users (Password: password123)
INSERT INTO `users` (`id`, `full_name`, `email`, `password`, `phone`, `initials`, `profile_photo`, `resident_type`, `user_type`, `meal_reminder`, `auto_booking`, `auth_token`, `created_at`, `updated_at`) VALUES
('usr_fazlul', 'Dr. Fazlul Hasan', 'fazlul.hasan@duet.edu.bd', '$2y$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '+880 1234 567890', 'FH', NULL, 'outside', 'teacher', 1, 0, 'token_fazlul_123456789', NOW(), NOW()),
('usr_admin', 'Canteen Administrator', 'admin@duet.edu.bd', '$2y$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm', '+880 1700 000000', 'CA', NULL, 'inside', 'officer', 0, 0, 'token_admin_123456789', NOW(), NOW());

-- Wallets (Fazlul Hasan: ৳1450 total deposited, ৳1090 spendable available balance)
INSERT INTO `wallets` (`user_id`, `total_balance`, `available_balance`, `currency`, `updated_at`) VALUES
('usr_fazlul', 1450.00, 1090.00, 'BDT', NOW()),
('usr_admin', 5000.00, 5000.00, 'BDT', NOW());

-- Notices
INSERT INTO `notices` (`id`, `category`, `tag`, `title`, `content`, `author_id`, `created_at`) VALUES
('not_1', 'dining', 'IMPORTANT', 'August Meal Rate Finalized', 'The meal rate for August 2026 has been calculated and finalized at ৳88.10. Balance adjustments will be completed automatically.', 'admin', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('not_2', 'maintenance', 'SYSTEM INFO', 'App Maintenance Notice', 'System maintenance scheduled this Friday from 11:00 PM to 2:00 AM. Meal booking will be momentarily paused during this window.', 'admin', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('not_3', 'dining', 'NEW UPDATE', 'Guest Booking Policy Update', 'Guest dinner bookings must now be submitted at least 24 hours in advance to guarantee portion availability.', 'admin', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Transactions
INSERT INTO `transactions` (`id`, `user_id`, `type`, `title`, `amount`, `sign`, `balance_after`, `reference_id`, `timestamp`) VALUES
('tx_1', 'usr_fazlul', 'recharge', 'Cash Recharge by Admin', 1450.00, 'positive', 1450.00, NULL, DATE_SUB(NOW(), INTERVAL 7 DAY)),
('tx_2', 'usr_fazlul', 'meal_deduction', 'Meal Booking (Lunch & Dinner)', 360.00, 'negative', 1090.00, 'book_seed_1', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Seed Bookings
INSERT INTO `bookings` (`id`, `user_id`, `start_date`, `end_date`, `include_lunch`, `include_dinner`, `guest_count`, `total_people`, `price_per_meal`, `total_cost`, `status`, `created_at`) VALUES
('book_seed_1', 'usr_fazlul', DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 1, 0, 1, 90.00, 360.00, 'active', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- Seed Meal History
INSERT INTO `meal_history` (`id`, `user_id`, `booking_id`, `date`, `meal_type`, `guest_count`, `participants`, `status`, `price`, `created_at`) VALUES
('hist_1', 'usr_fazlul', 'book_seed_1', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'lunch', 0, 'Self', 'consumed', 90.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('hist_2', 'usr_fazlul', 'book_seed_1', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'dinner', 0, 'Self', 'consumed', 90.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
('hist_3', 'usr_fazlul', 'book_seed_1', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'lunch', 0, 'Self', 'consumed', 90.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('hist_4', 'usr_fazlul', 'book_seed_1', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'dinner', 0, 'Self', 'auto_cancelled', 90.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
('hist_5', 'usr_fazlul', NULL, CURDATE(), 'lunch', 1, 'Self + 1 Guest', 'booked', 180.00, NOW());

-- Seed Daily Menu
INSERT INTO `daily_menus` (`id`, `date`, `lunch_venue`, `lunch_start`, `lunch_end`, `lunch_cutoff`, `lunch_items`, `lunch_max_guests`, `dinner_venue`, `dinner_start`, `dinner_end`, `dinner_cutoff_advance_hours`, `dinner_items`, `dinner_max_guests`) VALUES
(CONCAT('menu_', CURDATE()), CURDATE(), 'Main Canteen', '12:30', '14:00', '12:00', '["Chicken Biryani", "Salad", "Borhani"]', 3, 'Main Canteen', '19:30', '21:00', 24, '["Steamed Rice", "Fish Curry", "Dal"]', 3);

-- Seed Meal Rate
INSERT INTO `meal_rates` (`id`, `month`, `rate`, `is_finalized`, `updated_at`) VALUES
(CONCAT('rate_', DATE_FORMAT(CURDATE(), '%Y-%m')), DATE_FORMAT(CURDATE(), '%Y-%m'), 88.10, 1, NOW());
