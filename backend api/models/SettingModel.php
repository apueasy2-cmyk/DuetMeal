<?php
require_once __DIR__ . '/../config/Database.php';

class SettingModel {
    private static function initTable(): void {
        $pdo = Database::getConnection();
        $pdo->exec("
            CREATE TABLE IF NOT EXISTS `system_settings` (
                `setting_key` VARCHAR(64) NOT NULL PRIMARY KEY,
                `setting_value` TEXT NULL,
                `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
        ");

        // Seed defaults if empty
        $defaults = [
            'booking_service_enabled' => '1',
            'booking_service_message' => 'Meal booking service is active and open for university staff.',
            'flat_meal_rate' => '90.00',
            'fixed_meal_rate' => '90.00',
            'lunch_cutoff_time' => '12:00:00',
            'dinner_advance_hours' => '24',
            'canteen_name' => 'DUET Main Canteen',
            'canteen_phone' => '+880 1700 000000',
            'canteen_notice' => ''
        ];

        $stmt = $pdo->prepare("INSERT IGNORE INTO `system_settings` (`setting_key`, `setting_value`, `updated_at`) VALUES (:k, :v, NOW())");
        foreach ($defaults as $k => $v) {
            $stmt->execute(['k' => $k, 'v' => $v]);
        }
    }

    public static function getAll(): array {
        self::initTable();
        $pdo = Database::getConnection();
        $stmt = $pdo->query("SELECT setting_key, setting_value FROM system_settings");
        $rows = $stmt->fetchAll(PDO::FETCH_KEY_PAIR);

        return [
            'bookingServiceEnabled' => ($rows['booking_service_enabled'] ?? '1') === '1',
            'bookingServiceMessage' => $rows['booking_service_message'] ?? 'Meal booking service is currently open.',
            'flatMealRate' => (float)($rows['flat_meal_rate'] ?? 90.00),
            'fixedMealRate' => (float)($rows['fixed_meal_rate'] ?? 90.00),
            'lunchCutoffTime' => $rows['lunch_cutoff_time'] ?? '12:00:00',
            'dinnerAdvanceHours' => (int)($rows['dinner_advance_hours'] ?? 24),
            'canteenName' => $rows['canteen_name'] ?? 'DUET Main Canteen',
            'canteenPhone' => $rows['canteen_phone'] ?? '+880 1700 000000',
            'canteenNotice' => $rows['canteen_notice'] ?? ''
        ];
    }

    public static function get(string $key, $default = null) {
        self::initTable();
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT setting_value FROM system_settings WHERE setting_key = :k LIMIT 1");
        $stmt->execute(['k' => $key]);
        $val = $stmt->fetchColumn();
        return $val !== false ? $val : $default;
    }

    public static function set(string $key, $value): bool {
        self::initTable();
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("
            INSERT INTO system_settings (setting_key, setting_value, updated_at) 
            VALUES (:k, :v, NOW()) 
            ON DUPLICATE KEY UPDATE setting_value = :v2, updated_at = NOW()
        ");
        return $stmt->execute([
            'k' => $key,
            'v' => (string)$value,
            'v2' => (string)$value
        ]);
    }

    public static function updateMany(array $settings): array {
        self::initTable();
        $map = [
            'bookingServiceEnabled' => 'booking_service_enabled',
            'booking_service_enabled' => 'booking_service_enabled',
            'bookingServiceMessage' => 'booking_service_message',
            'booking_service_message' => 'booking_service_message',
            'flatMealRate' => 'flat_meal_rate',
            'flat_meal_rate' => 'flat_meal_rate',
            'fixedMealRate' => 'fixed_meal_rate',
            'fixed_meal_rate' => 'fixed_meal_rate',
            'lunchCutoffTime' => 'lunch_cutoff_time',
            'lunch_cutoff_time' => 'lunch_cutoff_time',
            'dinnerAdvanceHours' => 'dinner_advance_hours',
            'dinner_advance_hours' => 'dinner_advance_hours',
            'canteenName' => 'canteen_name',
            'canteen_name' => 'canteen_name',
            'canteenPhone' => 'canteen_phone',
            'canteen_phone' => 'canteen_phone',
            'canteenNotice' => 'canteen_notice',
            'canteen_notice' => 'canteen_notice',
        ];

        foreach ($settings as $key => $val) {
            if (isset($map[$key])) {
                $dbKey = $map[$key];
                if (is_bool($val)) {
                    $val = $val ? '1' : '0';
                }
                self::set($dbKey, $val);
            }
        }

        return self::getAll();
    }
}
