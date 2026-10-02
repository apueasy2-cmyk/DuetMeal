<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';

class WalletModel {
    public static function getWallet(string $userId): array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM wallets WHERE user_id = :userId LIMIT 1");
        $stmt->execute(['userId' => $userId]);
        $wallet = $stmt->fetch();

        if (!$wallet) {
            // Auto initialize wallet if missing
            $now = date('Y-m-d H:i:s');
            $insert = $pdo->prepare("INSERT INTO wallets (user_id, total_balance, available_balance, currency, updated_at) VALUES (:user_id, 0.00, 0.00, 'BDT', :updated_at)");
            $insert->execute(['user_id' => $userId, 'updated_at' => $now]);
            return [
                'userId' => $userId,
                'totalBalance' => 0.00,
                'availableBalance' => 0.00,
                'currency' => 'BDT'
            ];
        }

        return [
            'userId' => (string)$wallet['user_id'],
            'totalBalance' => (float)$wallet['total_balance'],
            'availableBalance' => (float)$wallet['available_balance'],
            'currency' => $wallet['currency'] ?? 'BDT'
        ];
    }

    public static function requestRecharge(string $userId, float $amount, ?string $note = null): array {
        $pdo = Database::getConnection();
        $requestId = Utils::generateId('req');
        $now = date('Y-m-d H:i:s');

        $stmt = $pdo->prepare("
            INSERT INTO recharge_requests (id, user_id, amount, status, admin_note, created_at)
            VALUES (:id, :user_id, :amount, 'pending', :note, :created_at)
        ");
        $stmt->execute([
            'id' => $requestId,
            'user_id' => $userId,
            'amount' => $amount,
            'note' => $note,
            'created_at' => $now
        ]);

        return [
            'requestId' => $requestId,
            'userId' => $userId,
            'amount' => $amount,
            'status' => 'pending',
            'createdAt' => $now
        ];
    }

    public static function approveRecharge(string $requestIdOrUserId, float $amount = 0, ?string $adminNote = null): array {
        $pdo = Database::getConnection();
        $now = date('Y-m-d H:i:s');

        // Check if argument is a recharge request ID or direct userId
        $stmt = $pdo->prepare("SELECT * FROM recharge_requests WHERE id = :id LIMIT 1");
        $stmt->execute(['id' => $requestIdOrUserId]);
        $req = $stmt->fetch();

        if ($req) {
            $userId = $req['user_id'];
            $amount = (float)$req['amount'];

            $updateReq = $pdo->prepare("
                UPDATE recharge_requests 
                SET status = 'approved', admin_note = :note, approved_at = :now 
                WHERE id = :id
            ");
            $updateReq->execute([
                'note' => $adminNote ?? 'Approved by Admin',
                'now' => $now,
                'id' => $req['id']
            ]);
            $refId = $req['id'];
        } else {
            $userId = $requestIdOrUserId;
            $refId = Utils::generateId('rec');
        }

        if ($amount <= 0) {
            throw new Exception("Recharge amount must be greater than zero.");
        }

        // Fetch current wallet
        $wallet = self::getWallet($userId);
        $newTotal = $wallet['totalBalance'] + $amount;
        $newAvailable = $wallet['availableBalance'] + $amount;

        // Update wallet
        $stmtWallet = $pdo->prepare("
            UPDATE wallets 
            SET total_balance = :total, available_balance = :avail, updated_at = :now 
            WHERE user_id = :user_id
        ");
        $stmtWallet->execute([
            'total' => $newTotal,
            'avail' => $newAvailable,
            'now' => $now,
            'user_id' => $userId
        ]);

        // Insert positive transaction
        $txId = Utils::generateId('tx');
        $stmtTx = $pdo->prepare("
            INSERT INTO transactions (id, user_id, type, title, amount, sign, balance_after, reference_id, timestamp)
            VALUES (:id, :user_id, 'recharge', 'Cash Recharge by Admin', :amount, 'positive', :balance_after, :ref_id, :timestamp)
        ");
        $stmtTx->execute([
            'id' => $txId,
            'user_id' => $userId,
            'amount' => $amount,
            'balance_after' => $newAvailable,
            'ref_id' => $refId,
            'timestamp' => $now
        ]);

        return [
            'userId' => $userId,
            'rechargeAmount' => $amount,
            'totalBalance' => $newTotal,
            'availableBalance' => $newAvailable,
            'currency' => 'BDT',
            'transactionId' => $txId
        ];
    }

    public static function deductBalance(string $userId, float $amount, string $title, ?string $refId = null): array {
        $pdo = Database::getConnection();
        $wallet = self::getWallet($userId);

        if ($wallet['availableBalance'] < $amount) {
            throw new Exception("Insufficient balance. Available: ৳" . number_format($wallet['availableBalance'], 2) . ", Required: ৳" . number_format($amount, 2));
        }

        $now = date('Y-m-d H:i:s');
        $newAvailable = $wallet['availableBalance'] - $amount;
        $newTotal = $wallet['totalBalance'] - $amount; // ✅ FIXED: calculate newTotal

        $stmtWallet = $pdo->prepare("
            UPDATE wallets 
            SET total_balance = :total, available_balance = :avail, updated_at = :now 
            WHERE user_id = :user_id
        ");
        $stmtWallet->execute([
            'total' => $newTotal,  // ✅ FIXED: update total_balance in DB
            'avail' => $newAvailable,
            'now' => $now,
            'user_id' => $userId
        ]);

        $txId = Utils::generateId('tx');
        $stmtTx = $pdo->prepare("
            INSERT INTO transactions (id, user_id, type, title, amount, sign, balance_after, reference_id, timestamp)
            VALUES (:id, :user_id, 'meal_deduction', :title, :amount, 'negative', :balance_after, :ref_id, :timestamp)
        ");
        $stmtTx->execute([
            'id' => $txId,
            'user_id' => $userId,
            'title' => $title,
            'amount' => $amount,
            'balance_after' => $newAvailable,
            'ref_id' => $refId,
            'timestamp' => $now
        ]);

        return [
            'userId' => $userId,
            'deductedAmount' => $amount,
            'totalBalance' => $newTotal,   // ✅ FIXED: return correct new total
            'availableBalance' => $newAvailable,
            'transactionId' => $txId
        ];
    }

    public static function refundBalance(string $userId, float $amount, string $title, ?string $refId = null): array {
        $pdo = Database::getConnection();
        $wallet = self::getWallet($userId);
        $now = date('Y-m-d H:i:s');
        $newAvailable = $wallet['availableBalance'] + $amount;
        $newTotal = $wallet['totalBalance'] + $amount; // ✅ FIXED: calculate newTotal

        $stmtWallet = $pdo->prepare("
            UPDATE wallets 
            SET total_balance = :total, available_balance = :avail, updated_at = :now 
            WHERE user_id = :user_id
        ");
        $stmtWallet->execute([
            'total' => $newTotal,  // ✅ FIXED: update total_balance in DB
            'avail' => $newAvailable,
            'now' => $now,
            'user_id' => $userId
        ]);

        $txId = Utils::generateId('tx');
        $stmtTx = $pdo->prepare("
            INSERT INTO transactions (id, user_id, type, title, amount, sign, balance_after, reference_id, timestamp)
            VALUES (:id, :user_id, 'refund', :title, :amount, 'positive', :balance_after, :ref_id, :timestamp)
        ");
        $stmtTx->execute([
            'id' => $txId,
            'user_id' => $userId,
            'title' => $title,
            'amount' => $amount,
            'balance_after' => $newAvailable,
            'ref_id' => $refId,
            'timestamp' => $now
        ]);

        return [
            'userId' => $userId,
            'refundedAmount' => $amount,
            'totalBalance' => $newTotal,   // ✅ FIXED: $newTotal now defined
            'availableBalance' => $newAvailable,
            'transactionId' => $txId
        ];
    }
}
