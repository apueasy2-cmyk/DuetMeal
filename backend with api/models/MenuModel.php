<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';

class MenuModel {
    public static function formatMenu(array $row): array {
        $lunchItems = !empty($row['lunch_items']) ? json_decode($row['lunch_items'], true) : ["Chicken Biryani", "Salad", "Borhani"];
        $dinnerItems = !empty($row['dinner_items']) ? json_decode($row['dinner_items'], true) : ["Steamed Rice", "Fish Curry", "Dal"];

        return [
            'date' => (string)$row['date'],
            'lunch' => [
                'venue' => (string)($row['lunch_venue'] ?? 'Main Canteen'),
                'startTime' => (string)($row['lunch_start'] ?? '12:30'),
                'endTime' => (string)($row['lunch_end'] ?? '14:00'),
                'cutoffTime' => (string)($row['lunch_cutoff'] ?? '12:00'),
                'items' => $lunchItems ?: ["Chicken Biryani", "Salad", "Borhani"],
                'maxGuests' => (int)($row['lunch_max_guests'] ?? 3)
            ],
            'dinner' => [
                'venue' => (string)($row['dinner_venue'] ?? 'Main Canteen'),
                'startTime' => (string)($row['dinner_start'] ?? '19:30'),
                'endTime' => (string)($row['dinner_end'] ?? '21:00'),
                'cutoffHoursInAdvance' => (int)($row['dinner_cutoff_advance_hours'] ?? 24),
                'items' => $dinnerItems ?: ["Steamed Rice", "Fish Curry", "Dal"],
                'maxGuests' => (int)($row['dinner_max_guests'] ?? 3)
            ]
        ];
    }

    public static function getTodayMenu(?string $date = null): array {
        $date = $date ?: date('Y-m-d');
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM daily_menus WHERE date = :date LIMIT 1");
        $stmt->execute(['date' => $date]);
        $row = $stmt->fetch();

        if (!$row) {
            // Return sensible default DUET canteen schedule for today
            return [
                'date' => $date,
                'lunch' => [
                    'venue' => 'Main Canteen',
                    'startTime' => '12:30',
                    'endTime' => '14:00',
                    'cutoffTime' => '12:00',
                    'items' => ['Chicken Biryani', 'Salad', 'Borhani'],
                    'maxGuests' => 3
                ],
                'dinner' => [
                    'venue' => 'Main Canteen',
                    'startTime' => '19:30',
                    'endTime' => '21:00',
                    'cutoffHoursInAdvance' => 24,
                    'items' => ['Steamed Rice', 'Fish Curry', 'Dal'],
                    'maxGuests' => 3
                ]
            ];
        }

        return self::formatMenu($row);
    }

    public static function getMonthlyMenu(string $month): array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM daily_menus WHERE date LIKE :monthPrefix ORDER BY date ASC");
        $stmt->execute(['monthPrefix' => $month . '%']);
        $rows = $stmt->fetchAll();

        if (empty($rows)) {
            // Provide today's or default menu template for the month
            return [self::getTodayMenu()];
        }

        return array_map([self::class, 'formatMenu'], $rows);
    }

    public static function saveMenu(string $date, array $lunchData, array $dinnerData): array {
        $pdo = Database::getConnection();
        $id = 'menu_' . $date;

        $stmt = $pdo->prepare("
            INSERT INTO daily_menus (
                id, date, lunch_venue, lunch_start, lunch_end, lunch_cutoff, lunch_items, lunch_max_guests,
                dinner_venue, dinner_start, dinner_end, dinner_cutoff_advance_hours, dinner_items, dinner_max_guests
            ) VALUES (
                :id, :date, :lunch_venue, :lunch_start, :lunch_end, :lunch_cutoff, :lunch_items, :lunch_max_guests,
                :dinner_venue, :dinner_start, :dinner_end, :dinner_cutoff_advance_hours, :dinner_items, :dinner_max_guests
            ) ON DUPLICATE KEY UPDATE
                lunch_venue = VALUES(lunch_venue),
                lunch_start = VALUES(lunch_start),
                lunch_end = VALUES(lunch_end),
                lunch_cutoff = VALUES(lunch_cutoff),
                lunch_items = VALUES(lunch_items),
                lunch_max_guests = VALUES(lunch_max_guests),
                dinner_venue = VALUES(dinner_venue),
                dinner_start = VALUES(dinner_start),
                dinner_end = VALUES(dinner_end),
                dinner_cutoff_advance_hours = VALUES(dinner_cutoff_advance_hours),
                dinner_items = VALUES(dinner_items),
                dinner_max_guests = VALUES(dinner_max_guests)
        ");

        $stmt->execute([
            'id' => $id,
            'date' => $date,
            'lunch_venue' => $lunchData['venue'] ?? 'Main Canteen',
            'lunch_start' => $lunchData['startTime'] ?? '12:30',
            'lunch_end' => $lunchData['endTime'] ?? '14:00',
            'lunch_cutoff' => $lunchData['cutoffTime'] ?? '12:00',
            'lunch_items' => json_encode($lunchData['items'] ?? ['Chicken Biryani', 'Salad', 'Borhani']),
            'lunch_max_guests' => $lunchData['maxGuests'] ?? 3,
            'dinner_venue' => $dinnerData['venue'] ?? 'Main Canteen',
            'dinner_start' => $dinnerData['startTime'] ?? '19:30',
            'dinner_end' => $dinnerData['endTime'] ?? '21:00',
            'dinner_cutoff_advance_hours' => $dinnerData['cutoffHoursInAdvance'] ?? 24,
            'dinner_items' => json_encode($dinnerData['items'] ?? ['Steamed Rice', 'Fish Curry', 'Dal']),
            'dinner_max_guests' => $dinnerData['maxGuests'] ?? 3
        ]);

        return self::getTodayMenu($date);
    }
}
