<?php
/**
 * DUET Meal - Nightly Ledger Audit Script
 * Validates wallet balances against immutable transaction histories.
 * Logs mismatches as alerts for Admin review.
 */
require_once __DIR__ . '/../config/Database.php';

echo "Starting Nightly Ledger Audit...\n";
$pdo = Database::getConnection();

// Get all users
$stmt = $pdo->query("SELECT id, full_name FROM users");
$users = $stmt->fetchAll(PDO::FETCH_ASSOC);

$mismatches = 0;
foreach ($users as $u) {
    $userId = $u['id'];
    
    // Get true sum from ledger
    $sumStmt = $pdo->prepare("
        SELECT SUM(
            CASE 
                WHEN sign = 'positive' THEN amount 
                WHEN sign = 'negative' THEN -amount 
                ELSE 0 
            END
        ) as true_balance 
        FROM transactions 
        WHERE user_id = :uid
    ");
    $sumStmt->execute(['uid' => $userId]);
    $trueBalance = (float)$sumStmt->fetchColumn();

    // Get current wallet record
    $walletStmt = $pdo->prepare("SELECT total_balance FROM wallets WHERE user_id = :uid LIMIT 1");
    $walletStmt->execute(['uid' => $userId]);
    $walletRecord = $walletStmt->fetch();
    $recordedBalance = $walletRecord ? (float)$walletRecord['total_balance'] : 0.0;

    // Check mismatch
    if (abs($trueBalance - $recordedBalance) > 0.01) {
        $mismatches++;
        $msg = "Ledger Mismatch! User: {$u['full_name']} (ID: {$userId}). Ledger says ৳{$trueBalance}, but Wallet says ৳{$recordedBalance}.";
        echo "[ALERT] $msg\n";
        
        // Insert into notices table for admin review (as a system alert)
        // Ensure this doesn't duplicate endlessly if run multiple times
        $checkAlert = $pdo->prepare("SELECT id FROM notices WHERE title LIKE '%Ledger Mismatch%' AND content LIKE :uid LIMIT 1");
        $checkAlert->execute(['uid' => "%{$userId}%"]);
        if (!$checkAlert->fetch()) {
            $insertAlert = $pdo->prepare("
                INSERT INTO notices (id, category, title, content, tag, created_at, updated_at) 
                VALUES (:id, 'system', 'Ledger Mismatch Detected', :msg, 'IMPORTANT', NOW(), NOW())
            ");
            $insertAlert->execute([
                'id' => 'alert_' . uniqid(),
                'msg' => $msg
            ]);
        }
    }
}

echo "Audit Complete. Found $mismatches mismatches.\n";
