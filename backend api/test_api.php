<?php

require_once __DIR__ . '/config/Database.php';
require_once __DIR__ . '/models/UserModel.php';
require_once __DIR__ . '/models/WalletModel.php';
require_once __DIR__ . '/models/BookingModel.php';
require_once __DIR__ . '/models/HistoryModel.php';
require_once __DIR__ . '/models/TransactionModel.php';
require_once __DIR__ . '/models/NoticeModel.php';
require_once __DIR__ . '/models/MenuModel.php';
require_once __DIR__ . '/models/SettlementModel.php';

$isCli = (php_sapi_name() === 'cli');

$testResults = [];
$startTimeTotal = microtime(true);

function runTest(string $title, string $endpoint, string $method, callable $callable) {
    global $testResults;
    $start = microtime(true);
    $status = 'PASSED';
    $error = null;
    $data = null;

    try {
        $data = $callable();
        if ($data === false) {
            $status = 'FAILED';
            $error = 'Assertion failed or returned false.';
        }
    } catch (Throwable $e) {
        $status = 'FAILED';
        $error = $e->getMessage();
    }
    $duration = round((microtime(true) - $start) * 1000, 2);

    $testResults[] = [
        'title' => $title,
        'endpoint' => $endpoint,
        'method' => $method,
        'status' => $status,
        'durationMs' => $duration,
        'error' => $error,
        'data' => $data
    ];
}

// --------------------------------------------------------------------------
// Execute Test Suite against MySQL
// --------------------------------------------------------------------------

// 1. MySQL Database Connection
runTest("Database Connection (MySQL)", "SYSTEM", "INTERNAL", function() {
    $pdo = Database::getConnection();
    return [
        'driver' => 'MYSQL',
        'status' => 'connected',
        'database' => 'duetmeal'
    ];
});

// 2. User Authentication (Login)
runTest("User Authentication (Login)", "POST /user/auth/login", "POST", function() {
    $user = UserModel::authenticate('fazlul.hasan@duet.edu.bd', 'password123');
    if (!$user) throw new Exception("Authentication failed. Make sure db.sql is imported.");
    return $user;
});

// 3. User Profile & Preferences
runTest("User Profile & Preference Update", "PUT /user/users/usr_fazlul", "PUT", function() {
    $updated = UserModel::updateProfile('usr_fazlul', [
        'preferences' => [
            'mealReminder' => true,
            'autoBooking' => false
        ]
    ]);
    if (!$updated) throw new Exception("Profile update failed");
    return $updated;
});

// 4. Wallet Balance Inquiry
runTest("Wallet Balance Inquiry", "GET /user/wallet/usr_fazlul", "GET", function() {
    return WalletModel::getWallet('usr_fazlul');
});

// 5. Wallet Recharge Request & Approval Flow
runTest("Wallet Recharge Flow", "POST /user/wallet/recharge/request & approve", "POST", function() {
    $req = WalletModel::requestRecharge('usr_fazlul', 500.00, 'Bank deposit');
    $approved = WalletModel::approveRecharge($req['requestId'], 500.00, 'Approved by Admin');
    return [
        'step1_request' => $req,
        'step2_approved' => $approved
    ];
});

// 6. Today's Canteen Menu
runTest("Today's Canteen Menu", "GET /user/meals/today", "GET", function() {
    return MenuModel::getTodayMenu();
});

// 7. Monthly Canteen Menu
runTest("Monthly Canteen Menu", "GET /user/meals/menu?month=" . date('Y-m'), "GET", function() {
    return MenuModel::getMonthlyMenu(date('Y-m'));
});

// 8. Meal Booking Creation (Prepaid Deduction)
runTest("Create Meal Booking (Prepaid)", "POST /user/bookings", "POST", function() {
    $futureStart = date('Y-m-d', strtotime('+3 days'));
    $futureEnd = date('Y-m-d', strtotime('+4 days'));

    $walletBefore = WalletModel::getWallet('usr_fazlul');
    $booking = BookingModel::createBooking('usr_fazlul', $futureStart, $futureEnd, true, true, 1);
    $walletAfter = WalletModel::getWallet('usr_fazlul');

    return [
        'booking' => $booking,
        'deductionSummary' => [
            'walletBefore' => $walletBefore['availableBalance'],
            'walletAfter' => $walletAfter['availableBalance'],
            'deductedAmount' => $booking['totalCost']
        ]
    ];
});

// 9. Meal Booking Cancellation & Refund
runTest("Cancel Meal Booking & Refund", "DELETE /user/bookings/{id}", "DELETE", function() {
    $futureStart = date('Y-m-d', strtotime('+6 days'));
    $booking = BookingModel::createBooking('usr_fazlul', $futureStart, $futureStart, true, false, 0);
    $walletBefore = WalletModel::getWallet('usr_fazlul');
    
    $cancelled = BookingModel::cancelBooking($booking['id'], 'usr_fazlul');
    $walletAfter = WalletModel::getWallet('usr_fazlul');

    return [
        'cancelledBooking' => $cancelled,
        'refundSummary' => [
            'walletBeforeRefund' => $walletBefore['availableBalance'],
            'walletAfterRefund' => $walletAfter['availableBalance'],
            'refundedAmount' => $cancelled['totalCost']
        ]
    ];
});

// 10. Meal Booking History
runTest("Meal History Records", "GET /user/history?userId=usr_fazlul", "GET", function() {
    $history = HistoryModel::getHistory('usr_fazlul', 'All', 1, 5);
    return [
        'totalFetched' => count($history),
        'records' => $history
    ];
});

// 11. Transaction History & Statistics
runTest("Transactions & Wallet Statistics", "GET /user/transactions?userId=usr_fazlul", "GET", function() {
    return TransactionModel::getTransactions('usr_fazlul', 'all', 1, 5);
});

// 12. Notices & Read Status
runTest("Notices Board & Mark Read", "GET /user/notices & PUT /user/notices/{id}/read", "GET/PUT", function() {
    $noticesResult = NoticeModel::getNotices('usr_fazlul');
    if (!empty($noticesResult['notices'])) {
        $firstId = $noticesResult['notices'][0]['id'];
        NoticeModel::markAsRead($firstId, 'usr_fazlul');
    }
    return NoticeModel::getNotices('usr_fazlul');
});

// 13. Settlement & Rate Finalization
runTest("Monthly Settlement Calculation", "POST /user/meal-rate & /user/settlement/run", "POST", function() {
    $month = date('Y-m');
    SettlementModel::setMealRate($month, 88.10, true);
    return SettlementModel::runSettlement($month);
});

$totalDuration = round((microtime(true) - $startTimeTotal) * 1000, 2);
$passedCount = count(array_filter($testResults, fn($t) => $t['status'] === 'PASSED'));
$failedCount = count($testResults) - $passedCount;

if ($isCli) {
    echo "\n" . str_repeat("=", 80) . "\n";
    echo "  DUET MEAL API — TEST SUITE & MYSQL DATA REPORT\n";
    echo str_repeat("=", 80) . "\n\n";

    foreach ($testResults as $idx => $t) {
        $num = str_pad($idx + 1, 2, ' ', STR_PAD_LEFT);
        $statusTag = ($t['status'] === 'PASSED') ? "\033[32m[PASSED]\033[0m" : "\033[31m[FAILED]\033[0m";
        
        echo "{$num}. {$statusTag} {$t['title']} ({$t['method']} {$t['endpoint']}) - {$t['durationMs']}ms\n";
        echo str_repeat("-", 80) . "\n";
        
        if ($t['status'] === 'FAILED') {
            echo "   Error: " . $t['error'] . "\n";
        } else {
            $json = json_encode($t['data'], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
            $indented = preg_replace('/^/m', '   ', $json);
            echo $indented . "\n";
        }
        echo "\n";
    }

    echo str_repeat("=", 80) . "\n";
    echo "  SUMMARY: Total: " . count($testResults) . " | Passed: {$passedCount} | Failed: {$failedCount} | Duration: {$totalDuration}ms\n";
    echo str_repeat("=", 80) . "\n\n";
    exit;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DUET Meal API — Test Suite</title>
    <style>
        :root {
            --bg: #090d16;
            --card-bg: #131b2e;
            --border: #1e293b;
            --primary: #38bdf8;
            --success: #10b981;
            --danger: #ef4444;
            --text: #f8fafc;
            --text-muted: #94a3b8;
            --code-bg: #0b1120;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, monospace; }
        body { background: var(--bg); color: var(--text); padding: 30px; line-height: 1.5; }
        .container { max-width: 1000px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; border-bottom: 1px solid var(--border); padding-bottom: 20px; flex-wrap: wrap; gap: 15px; }
        .title-area h1 { font-size: 22px; font-weight: 700; color: var(--primary); }
        .title-area p { color: var(--text-muted); font-size: 13px; margin-top: 4px; }
        .stats-bar { display: flex; gap: 10px; }
        .stat-badge { padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; background: var(--card-bg); border: 1px solid var(--border); }
        .stat-passed { border-color: rgba(16, 185, 129, 0.4); color: var(--success); }
        .stat-failed { border-color: rgba(239, 68, 68, 0.4); color: var(--danger); }
        .actions { margin-bottom: 20px; display: flex; gap: 10px; }
        .btn { background: var(--primary); color: #090d16; border: none; padding: 8px 14px; border-radius: 6px; font-weight: 600; font-size: 13px; cursor: pointer; text-decoration: none; }
        .btn-outline { background: transparent; color: var(--text); border: 1px solid var(--border); }
        .test-list { display: flex; flex-direction: column; gap: 12px; }
        .test-card { background: var(--card-bg); border: 1px solid var(--border); border-radius: 8px; overflow: hidden; }
        .test-header { padding: 12px 16px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; user-select: none; }
        .test-title-grp { display: flex; align-items: center; gap: 10px; }
        .badge { font-size: 11px; font-weight: 700; padding: 2px 6px; border-radius: 4px; }
        .badge-passed { background: rgba(16, 185, 129, 0.15); color: #34d399; }
        .badge-failed { background: rgba(239, 68, 68, 0.15); color: #f87171; }
        .badge-method { background: #1e293b; color: #38bdf8; font-family: monospace; }
        .test-name { font-size: 14px; font-weight: 600; }
        .test-endpoint { font-size: 12px; color: var(--text-muted); font-family: monospace; }
        .test-meta { font-size: 12px; color: var(--text-muted); }
        .test-body { padding: 14px 16px; border-top: 1px solid var(--border); background: var(--code-bg); }
        pre { font-family: 'Consolas', monospace; font-size: 12px; color: #e2e8f0; overflow-x: auto; white-space: pre-wrap; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="title-area">
                <h1>🍱 DUET Meal API — Test Suite (MySQL)</h1>
                <p>Verify all API endpoints & inspect MySQL database responses.</p>
            </div>
            <div class="stats-bar">
                <div class="stat-badge stat-passed">Passed: <?= $passedCount ?></div>
                <?php if ($failedCount > 0): ?>
                    <div class="stat-badge stat-failed">Failed: <?= $failedCount ?></div>
                <?php endif; ?>
                <div class="stat-badge"><?= $totalDuration ?>ms</div>
            </div>
        </div>

        <div class="actions">
            <button class="btn" onclick="location.reload()">🔄 Re-run Tests</button>
            <button class="btn btn-outline" onclick="toggleAll()">↕️ Expand / Collapse All</button>
        </div>

        <div class="test-list">
            <?php foreach ($testResults as $i => $test): ?>
                <div class="test-card">
                    <div class="test-header" onclick="toggleCard(<?= $i ?>)">
                        <div class="test-title-grp">
                            <span class="badge <?= $test['status'] === 'PASSED' ? 'badge-passed' : 'badge-failed' ?>"><?= $test['status'] ?></span>
                            <span class="badge badge-method"><?= htmlspecialchars($test['method']) ?></span>
                            <div>
                                <div class="test-name"><?= htmlspecialchars($test['title']) ?></div>
                                <div class="test-endpoint"><?= htmlspecialchars($test['endpoint']) ?></div>
                            </div>
                        </div>
                        <div class="test-meta">
                            <span><?= $test['durationMs'] ?>ms</span>
                            <span id="icon-<?= $i ?>">▼</span>
                        </div>
                    </div>
                    <div class="test-body" id="body-<?= $i ?>">
                        <?php if ($test['status'] === 'FAILED'): ?>
                            <div style="color: var(--danger); font-weight: 600; margin-bottom: 8px;">Error: <?= htmlspecialchars($test['error']) ?></div>
                        <?php endif; ?>
                        <pre><?= htmlspecialchars(json_encode($test['data'], JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE)) ?></pre>
                    </div>
                </div>
            <?php endforeach; ?>
        </div>
    </div>

    <script>
        function toggleCard(idx) {
            const body = document.getElementById('body-' + idx);
            const icon = document.getElementById('icon-' + idx);
            if (body.style.display === 'none') {
                body.style.display = 'block';
                icon.textContent = '▼';
            } else {
                body.style.display = 'none';
                icon.textContent = '▶';
            }
        }

        let allExpanded = true;
        function toggleAll() {
            allExpanded = !allExpanded;
            const count = <?= count($testResults) ?>;
            for (let i = 0; i < count; i++) {
                const body = document.getElementById('body-' + i);
                const icon = document.getElementById('icon-' + i);
                if (body && icon) {
                    body.style.display = allExpanded ? 'block' : 'none';
                    icon.textContent = allExpanded ? '▼' : '▶';
                }
            }
        }
    </script>
</body>
</html>
