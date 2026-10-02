<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/WalletModel.php';

class TransactionModel {
    public static function formatTransaction(array $row): array {
        return [
            'id' => (string)$row['id'],
            'userId' => (string)$row['user_id'],
            'type' => (string)$row['type'],
            'title' => (string)$row['title'],
            'amount' => (float)$row['amount'],
            'sign' => (string)$row['sign'],
            'balanceAfter' => (float)$row['balance_after'],
            'timestamp' => (string)$row['timestamp']
        ];
    }

    public static function getTransactions(
        string $userId,
        ?string $type = null,
        int $page = 1,
        int $limit = 20
    ): array {
        $pdo = Database::getConnection();
        $sql = "SELECT * FROM transactions WHERE user_id = :userId";
        $params = ['userId' => $userId];

        if (!empty($type) && strtolower($type) !== 'all') {
            $normalizedType = strtolower($type);
            $sql .= " AND type = :type";
            $params['type'] = $normalizedType;
        }

        $sql .= " ORDER BY timestamp DESC";

        $offset = max(0, ($page - 1) * $limit);
        $sql .= " LIMIT {$limit} OFFSET {$offset}";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $rows = $stmt->fetchAll();

        $transactions = array_map([self::class, 'formatTransaction'], $rows);

        // Calculate summary statistics
        $statsStmt = $pdo->prepare("
            SELECT 
                COALESCE(SUM(CASE WHEN sign = 'negative' OR type = 'meal_deduction' THEN amount ELSE 0 END), 0) as total_spending,
                COALESCE(SUM(CASE WHEN sign = 'positive' OR type = 'recharge' THEN amount ELSE 0 END), 0) as total_recharges
            FROM transactions 
            WHERE user_id = :userId
        ");
        $statsStmt->execute(['userId' => $userId]);
        $stats = $statsStmt->fetch();

        $wallet = WalletModel::getWallet($userId);

        return [
            'summary' => [
                'totalSpending' => (float)$stats['total_spending'],
                'totalRecharges' => (float)$stats['total_recharges'],
                'currentBalance' => (float)$wallet['availableBalance'],
                'currency' => 'BDT'
            ],
            'transactions' => $transactions
        ];
    }
}
