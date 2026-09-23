<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/WalletModel.php';

class SettlementModel {
    public static function setMealRate(string $month, float $rate, bool $isFinalized = true): array {
        $pdo = Database::getConnection();
        $id = 'rate_' . $month;
        $now = date('Y-m-d H:i:s');

        $stmt = $pdo->prepare("
            INSERT INTO meal_rates (id, month, rate, is_finalized, updated_at)
            VALUES (:id, :month, :rate, :is_finalized, :updated_at)
        ");
        
        try {
            $stmt->execute([
                'id' => $id,
                'month' => $month,
                'rate' => $rate,
                'is_finalized' => $isFinalized ? 1 : 0,
                'updated_at' => $now
            ]);
        } catch (Throwable $e) {
            $update = $pdo->prepare("
                UPDATE meal_rates SET rate = :rate, is_finalized = :is_finalized, updated_at = :updated_at
                WHERE month = :month
            ");
            $update->execute([
                'rate' => $rate,
                'is_finalized' => $isFinalized ? 1 : 0,
                'updated_at' => $now,
                'month' => $month
            ]);
        }

        return [
            'month' => $month,
            'rate' => $rate,
            'isFinalized' => $isFinalized,
            'updatedAt' => $now
        ];
    }

    public static function runSettlement(string $month): array {
        $pdo = Database::getConnection();

        // 1. Get finalized rate
        $stmt = $pdo->prepare("SELECT rate FROM meal_rates WHERE month = :month AND is_finalized = 1 LIMIT 1");
        $stmt->execute(['month' => $month]);
        $rateRow = $stmt->fetch();

        if (!$rateRow) {
            throw new Exception("No finalized meal rate found for month: {$month}. Please set meal rate first.");
        }

        $finalRate = (float)$rateRow['rate'];
        $flatRate = 90.00;
        $adjustmentPerPersonMeal = $flatRate - $finalRate; // e.g. 90.00 - 88.10 = +1.90 refund per meal

        // 2. Query consumed meals for the month grouped by user
        $historyStmt = $pdo->prepare("
            SELECT user_id, COUNT(*) as meal_count, SUM(guest_count + 1) as total_portions
            FROM meal_history
            WHERE date LIKE :monthPrefix AND status = 'consumed'
            GROUP BY user_id
        ");
        $historyStmt->execute(['monthPrefix' => $month . '%']);
        $userConsumptions = $historyStmt->fetchAll();

        $results = [];
        $now = date('Y-m-d H:i:s');

        foreach ($userConsumptions as $row) {
            $userId = $row['user_id'];
            $portions = (int)$row['total_portions'];
            $totalAdjustment = round($portions * $adjustmentPerPersonMeal, 2);

            if ($totalAdjustment != 0) {
                $wallet = WalletModel::getWallet($userId);
                $newAvailable = $wallet['availableBalance'] + $totalAdjustment;
                $newTotal = $wallet['totalBalance'] + ($totalAdjustment > 0 ? $totalAdjustment : 0);

                // Update wallet
                $updateWallet = $pdo->prepare("
                    UPDATE wallets 
                    SET available_balance = :avail, total_balance = :total, updated_at = :now 
                    WHERE user_id = :user_id
                ");
                $updateWallet->execute([
                    'avail' => $newAvailable,
                    'total' => $newTotal,
                    'now' => $now,
                    'user_id' => $userId
                ]);

                // Insert settlement transaction
                $sign = $totalAdjustment >= 0 ? 'positive' : 'negative';
                $txId = Utils::generateId('tx');
                $pdo->prepare("
                    INSERT INTO transactions (id, user_id, type, title, amount, sign, balance_after, reference_id, timestamp)
                    VALUES (:id, :user_id, 'settlement', :title, :amount, :sign, :balance_after, :ref_id, :timestamp)
                ")->execute([
                    'id' => $txId,
                    'user_id' => $userId,
                    'title' => "Monthly Settlement Adjustment ({$month}) @ ৳{$finalRate}/meal",
                    'amount' => abs($totalAdjustment),
                    'sign' => $sign,
                    'balance_after' => $newAvailable,
                    'ref_id' => 'settle_' . $month,
                    'timestamp' => $now
                ]);

                $results[] = [
                    'userId' => $userId,
                    'consumedPortions' => $portions,
                    'adjustmentAmount' => $totalAdjustment,
                    'newBalance' => $newAvailable
                ];
            }
        }

        return [
            'month' => $month,
            'finalRate' => $finalRate,
            'adjustmentPerPortion' => $adjustmentPerPersonMeal,
            'settledUsers' => count($results),
            'details' => $results
        ];
    }
}
