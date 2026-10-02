<?php
require_once __DIR__ . '/../config/Database.php';

class HistoryModel {
    public static function formatHistory(array $row): array {
        return [
            'id' => (string)$row['id'],
            'userId' => (string)$row['user_id'],
            'date' => (string)$row['date'],
            'mealType' => strtolower($row['meal_type']),
            'participants' => (string)$row['participants'],
            'status' => strtolower(str_replace('-', '_', $row['status'])),
            'price' => (float)$row['price']
        ];
    }

    public static function getHistory(
        string $userId,
        ?string $filter = null,
        int $page = 1,
        int $limit = 20
    ): array {
        $pdo = Database::getConnection();
        $sql = "SELECT * FROM meal_history WHERE user_id = :userId";
        $params = ['userId' => $userId];

        if (!empty($filter) && strtolower($filter) !== 'all') {
            $normalized = strtolower(str_replace(['-', ' '], '_', $filter));
            $sql .= " AND status = :status";
            $params['status'] = $normalized;
        }

        $sql .= " ORDER BY date DESC, created_at DESC";

        $offset = max(0, ($page - 1) * $limit);
        $sql .= " LIMIT {$limit} OFFSET {$offset}";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $rows = $stmt->fetchAll();

        return array_map([self::class, 'formatHistory'], $rows);
    }
}
