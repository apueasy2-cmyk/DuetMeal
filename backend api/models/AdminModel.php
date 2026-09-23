<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/UserModel.php';
require_once __DIR__ . '/WalletModel.php';
require_once __DIR__ . '/SettingModel.php';
require_once __DIR__ . '/NoticeModel.php';
require_once __DIR__ . '/MenuModel.php';
require_once __DIR__ . '/SettlementModel.php';

class AdminModel {

    /**
     * Dashboard KPI Stats
     */
    public static function getDashboardStats(): array {
        $pdo = Database::getConnection();
        $today = date('Y-m-d');
        $thisMonth = date('Y-m');

        // 1. Booking Switch & Global Settings
        $settings = SettingModel::getAll();

        // 2. Today's Meal Bookings Breakdown
        $stmtMeal = $pdo->prepare("
            SELECT 
                COALESCE(SUM(CASE WHEN meal_type = 'lunch' AND status = 'booked' THEN 1 + guest_count ELSE 0 END), 0) as lunch_booked_portions,
                COALESCE(SUM(CASE WHEN meal_type = 'lunch' AND status = 'consumed' THEN 1 + guest_count ELSE 0 END), 0) as lunch_consumed_portions,
                COALESCE(SUM(CASE WHEN meal_type = 'dinner' AND status = 'booked' THEN 1 + guest_count ELSE 0 END), 0) as dinner_booked_portions,
                COALESCE(SUM(CASE WHEN meal_type = 'dinner' AND status = 'consumed' THEN 1 + guest_count ELSE 0 END), 0) as dinner_consumed_portions,
                COALESCE(SUM(1 + guest_count), 0) as total_today_portions,
                COALESCE(SUM(price), 0) as total_today_meal_value
            FROM meal_history
            WHERE date = :today
        ");
        $stmtMeal->execute(['today' => $today]);
        $mealStats = $stmtMeal->fetch();

        // 3. Today's Cash Flow (Recharges vs Deductions vs Settlements)
        $stmtCash = $pdo->prepare("
            SELECT
                COALESCE(SUM(CASE WHEN type = 'recharge' AND sign = 'positive' THEN amount ELSE 0 END), 0) as today_recharges,
                COALESCE(SUM(CASE WHEN type = 'meal_deduction' THEN amount ELSE 0 END), 0) as today_meal_deductions,
                COUNT(*) as today_tx_count
            FROM transactions
            WHERE DATE(timestamp) = :today
        ");
        $stmtCash->execute(['today' => $today]);
        $cashStats = $stmtCash->fetch();

        // 4. Monthly Cash Stats
        $stmtMonth = $pdo->prepare("
            SELECT
                COALESCE(SUM(CASE WHEN type = 'recharge' AND sign = 'positive' THEN amount ELSE 0 END), 0) as month_recharges,
                COALESCE(SUM(CASE WHEN type = 'meal_deduction' THEN amount ELSE 0 END), 0) as month_deductions
            FROM transactions
            WHERE timestamp LIKE :monthPrefix
        ");
        $stmtMonth->execute(['monthPrefix' => $thisMonth . '%']);
        $monthStats = $stmtMonth->fetch();

        // 5. Total Users & Total System Balances
        $stmtUsers = $pdo->query("
            SELECT 
                COUNT(*) as total_users,
                SUM(CASE WHEN user_type = 'teacher' THEN 1 ELSE 0 END) as total_teachers,
                SUM(CASE WHEN user_type = 'officer' THEN 1 ELSE 0 END) as total_officers,
                SUM(CASE WHEN resident_type = 'inside' THEN 1 ELSE 0 END) as total_inside_residents
            FROM users
        ");
        $userCounts = $stmtUsers->fetch();

        $stmtWallets = $pdo->query("
            SELECT 
                COALESCE(SUM(available_balance), 0) as total_available_wallet_funds,
                COALESCE(SUM(total_balance), 0) as total_lifetime_deposits
            FROM wallets
        ");
        $walletCounts = $stmtWallets->fetch();

        // 6. Pending Recharge Requests Count
        $stmtReqs = $pdo->query("SELECT COUNT(*) FROM recharge_requests WHERE status = 'pending'");
        $pendingRecharges = (int)$stmtReqs->fetchColumn();

        // 7. Recent Transactions (last 8)
        $stmtRecentTx = $pdo->query("
            SELECT t.*, u.full_name, u.email, u.user_type
            FROM transactions t
            LEFT JOIN users u ON t.user_id = u.id
            ORDER BY t.timestamp DESC
            LIMIT 8
        ");
        $recentTransactions = $stmtRecentTx->fetchAll();

        // 8. Recent Bookings (last 8)
        $stmtRecentBook = $pdo->query("
            SELECT b.*, u.full_name, u.email
            FROM bookings b
            LEFT JOIN users u ON b.user_id = u.id
            ORDER BY b.created_at DESC
            LIMIT 8
        ");
        $recentBookings = $stmtRecentBook->fetchAll();

        return [
            'settings' => $settings,
            'today' => [
                'date' => $today,
                'lunchBooked' => (int)$mealStats['lunch_booked_portions'],
                'lunchConsumed' => (int)$mealStats['lunch_consumed_portions'],
                'dinnerBooked' => (int)$mealStats['dinner_booked_portions'],
                'dinnerConsumed' => (int)$mealStats['dinner_consumed_portions'],
                'totalPortions' => (int)$mealStats['total_today_portions'],
                'totalMealValue' => (float)$mealStats['total_today_meal_value'],
                'rechargesCash' => (float)$cashStats['today_recharges'],
                'mealDeductionsCash' => (float)$cashStats['today_meal_deductions'],
                'netCashDifference' => (float)($cashStats['today_recharges'] - $cashStats['today_meal_deductions']),
                'transactionCount' => (int)$cashStats['today_tx_count']
            ],
            'month' => [
                'month' => $thisMonth,
                'totalRecharges' => (float)$monthStats['month_recharges'],
                'totalDeductions' => (float)$monthStats['month_deductions']
            ],
            'users' => [
                'total' => (int)$userCounts['total_users'],
                'teachers' => (int)$userCounts['total_teachers'],
                'officers' => (int)$userCounts['total_officers'],
                'inside' => (int)$userCounts['total_inside_residents'],
                'pendingRecharges' => $pendingRecharges
            ],
            'finance' => [
                'totalWalletFunds' => (float)$walletCounts['total_available_wallet_funds'],
                'totalDeposits' => (float)$walletCounts['total_lifetime_deposits'],
                'currency' => 'BDT (৳)'
            ],
            'recentTransactions' => $recentTransactions,
            'recentBookings' => $recentBookings
        ];
    }

    /**
     * Daily Cash Log & Audit Breakdown
     */
    public static function getDailyCash(?string $date = null, ?string $startDate = null, ?string $endDate = null): array {
        $pdo = Database::getConnection();
        $date = $date ?: date('Y-m-d');

        $whereClause = "WHERE DATE(t.timestamp) = :date";
        $params = ['date' => $date];

        if ($startDate && $endDate) {
            $whereClause = "WHERE DATE(t.timestamp) BETWEEN :sdate AND :edate";
            $params = ['sdate' => $startDate, 'edate' => $endDate];
        }

        // Summary of this period
        $summaryStmt = $pdo->prepare("
            SELECT 
                COALESCE(SUM(CASE WHEN t.type = 'recharge' AND t.sign = 'positive' THEN t.amount ELSE 0 END), 0) as total_recharge_cash,
                COALESCE(SUM(CASE WHEN t.type = 'meal_deduction' THEN t.amount ELSE 0 END), 0) as total_meal_deduction,
                COALESCE(SUM(CASE WHEN t.type = 'settlement' AND t.sign = 'positive' THEN t.amount ELSE 0 END), 0) as total_settlement_refunds,
                COALESCE(SUM(CASE WHEN t.type = 'settlement' AND t.sign = 'negative' THEN t.amount ELSE 0 END), 0) as total_settlement_charges,
                COUNT(*) as transaction_count
            FROM transactions t
            {$whereClause}
        ");
        $summaryStmt->execute($params);
        $summary = $summaryStmt->fetch();

        // Meal stats for this date
        $mealStmt = $pdo->prepare("
            SELECT 
                COALESCE(SUM(CASE WHEN meal_type = 'lunch' THEN 1 + guest_count ELSE 0 END), 0) as lunch_portions,
                COALESCE(SUM(CASE WHEN meal_type = 'dinner' THEN 1 + guest_count ELSE 0 END), 0) as dinner_portions,
                COALESCE(SUM(price), 0) as meal_booking_cost
            FROM meal_history
            WHERE date = :date
        ");
        $mealStmt->execute(['date' => $date]);
        $mealStats = $mealStmt->fetch();

        // Detailed Transactions Table
        $txStmt = $pdo->prepare("
            SELECT t.*, u.full_name, u.email, u.phone, u.user_type, u.resident_type
            FROM transactions t
            LEFT JOIN users u ON t.user_id = u.id
            {$whereClause}
            ORDER BY t.timestamp DESC
        ");
        $txStmt->execute($params);
        $transactions = $txStmt->fetchAll();

        // Day by Day Cash Breakdown for past 7 days
        $pastDaysStmt = $pdo->query("
            SELECT 
                DATE(timestamp) as tx_date,
                COALESCE(SUM(CASE WHEN type = 'recharge' AND sign = 'positive' THEN amount ELSE 0 END), 0) as recharges,
                COALESCE(SUM(CASE WHEN type = 'meal_deduction' THEN amount ELSE 0 END), 0) as deductions,
                COUNT(*) as tx_count
            FROM transactions
            WHERE timestamp >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            GROUP BY DATE(timestamp)
            ORDER BY tx_date DESC
        ");
        $past7Days = $pastDaysStmt->fetchAll();

        return [
            'queryDate' => $date,
            'summary' => [
                'totalCashInflow' => (float)$summary['total_recharge_cash'],
                'totalMealDeductions' => (float)$summary['total_meal_deduction'],
                'totalSettlementRefunds' => (float)$summary['total_settlement_refunds'],
                'totalSettlementCharges' => (float)$summary['total_settlement_charges'],
                'netBalanceShift' => (float)($summary['total_recharge_cash'] - $summary['total_meal_deduction']),
                'transactionCount' => (int)$summary['transaction_count'],
                'lunchPortions' => (int)($mealStats['lunch_portions'] ?? 0),
                'dinnerPortions' => (int)($mealStats['dinner_portions'] ?? 0),
                'totalMealBookingCost' => (float)($mealStats['meal_booking_cost'] ?? 0)
            ],
            'past7Days' => $past7Days,
            'transactions' => $transactions
        ];
    }

    /**
     * User Directory with Wallets & Statistics
     */
    public static function getAllUsers(?string $search = null, ?string $userType = null, ?string $residentType = null, int $page = 1, int $limit = 50): array {
        $pdo = Database::getConnection();
        $sql = "
            SELECT 
                u.id, u.full_name, u.email, u.phone, u.initials, u.profile_photo,
                u.resident_type, u.user_type, u.meal_reminder, u.auto_booking, u.created_at,
                w.total_balance, w.available_balance, w.currency,
                (SELECT COUNT(*) FROM meal_history WHERE user_id = u.id AND status = 'consumed') as consumed_meals_count,
                (SELECT COUNT(*) FROM bookings WHERE user_id = u.id AND status = 'active') as active_bookings_count
            FROM users u
            LEFT JOIN wallets w ON u.id = w.user_id
            WHERE 1=1
        ";
        $params = [];

        if (!empty($search)) {
            $sql .= " AND (u.full_name LIKE :search OR u.email LIKE :search OR u.phone LIKE :search OR u.id LIKE :search)";
            $params['search'] = '%' . trim($search) . '%';
        }

        if (!empty($userType) && strtolower($userType) !== 'all') {
            $sql .= " AND u.user_type = :userType";
            $params['userType'] = strtolower($userType);
        }

        if (!empty($residentType) && strtolower($residentType) !== 'all') {
            $sql .= " AND u.resident_type = :residentType";
            $params['residentType'] = strtolower($residentType);
        }

        // Count query
        $countSql = preg_replace('/SELECT[\s\S]+?FROM users u/i', 'SELECT COUNT(*) FROM users u', $sql);
        // Clean out subqueries for count performance
        $countSql = "SELECT COUNT(*) FROM users u WHERE 1=1";
        if (!empty($search)) $countSql .= " AND (u.full_name LIKE :search OR u.email LIKE :search OR u.phone LIKE :search OR u.id LIKE :search)";
        if (!empty($userType) && strtolower($userType) !== 'all') $countSql .= " AND u.user_type = :userType";
        if (!empty($residentType) && strtolower($residentType) !== 'all') $countSql .= " AND u.resident_type = :residentType";

        $countStmt = $pdo->prepare($countSql);
        $countStmt->execute($params);
        $totalUsers = (int)$countStmt->fetchColumn();

        $sql .= " ORDER BY u.created_at DESC";
        $offset = max(0, ($page - 1) * $limit);
        $sql .= " LIMIT {$limit} OFFSET {$offset}";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $users = $stmt->fetchAll();

        return [
            'total' => $totalUsers,
            'page' => $page,
            'limit' => $limit,
            'users' => array_map(function($u) {
                return [
                    'id' => $u['id'],
                    'fullName' => $u['full_name'],
                    'email' => $u['email'],
                    'phone' => $u['phone'] ?? '',
                    'initials' => $u['initials'] ?: Utils::calculateInitials($u['full_name']),
                    'profilePhoto' => $u['profile_photo'],
                    'residentType' => $u['resident_type'],
                    'userType' => $u['user_type'],
                    'mealReminder' => (bool)$u['meal_reminder'],
                    'autoBooking' => (bool)$u['auto_booking'],
                    'createdAt' => $u['created_at'],
                    'wallet' => [
                        'totalBalance' => (float)($u['total_balance'] ?? 0),
                        'availableBalance' => (float)($u['available_balance'] ?? 0),
                        'currency' => $u['currency'] ?? 'BDT'
                    ],
                    'stats' => [
                        'consumedMeals' => (int)($u['consumed_meals_count'] ?? 0),
                        'activeBookings' => (int)($u['active_bookings_count'] ?? 0)
                    ]
                ];
            }, $users)
        ];
    }

    /**
     * Create New User Account by Admin
     */
    public static function createUser(array $data): array {
        $pdo = Database::getConnection();

        $fullName = trim($data['fullName'] ?? $data['full_name'] ?? '');
        $email = trim(strtolower($data['email'] ?? ''));
        $password = $data['password'] ?? 'password123';
        $phone = trim($data['phone'] ?? '');
        $residentType = $data['residentType'] ?? $data['resident_type'] ?? 'outside';
        $userType = $data['userType'] ?? $data['user_type'] ?? 'teacher';
        $initialBalance = (float)($data['initialBalance'] ?? $data['balance'] ?? 0);

        if (empty($fullName) || empty($email)) {
            throw new Exception("Full Name and Email are required.");
        }

        // Check if email already registered
        $existing = UserModel::findByEmail($email);
        if ($existing) {
            throw new Exception("A user with email '{$email}' already exists.");
        }

        // Generate ID
        $cleanEmailPrefix = preg_replace('/[^a-z0-9]/', '', explode('@', $email)[0]);
        $userId = 'usr_' . ($cleanEmailPrefix ?: Utils::generateId());
        
        // Ensure ID is unique
        if (UserModel::findById($userId)) {
            $userId .= '_' . substr(bin2hex(random_bytes(3)), 0, 4);
        }

        $initials = Utils::calculateInitials($fullName);
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
        $now = date('Y-m-d H:i:s');

        $stmt = $pdo->prepare("
            INSERT INTO users (
                id, full_name, email, password, phone, initials, 
                resident_type, user_type, meal_reminder, auto_booking, created_at, updated_at
            ) VALUES (
                :id, :full_name, :email, :password, :phone, :initials,
                :resident_type, :user_type, 1, 0, :created_at, :updated_at
            )
        ");

        $stmt->execute([
            'id' => $userId,
            'full_name' => $fullName,
            'email' => $email,
            'password' => $hashedPassword,
            'phone' => $phone,
            'initials' => $initials,
            'resident_type' => $residentType,
            'user_type' => $userType,
            'created_at' => $now,
            'updated_at' => $now
        ]);

        // Initialize Wallet
        $stmtWallet = $pdo->prepare("
            INSERT INTO wallets (user_id, total_balance, available_balance, currency, updated_at)
            VALUES (:user_id, :total, :avail, 'BDT', :now)
        ");
        $stmtWallet->execute([
            'user_id' => $userId,
            'total' => $initialBalance,
            'avail' => $initialBalance,
            'now' => $now
        ]);

        if ($initialBalance > 0) {
            $txId = Utils::generateId('tx');
            $pdo->prepare("
                INSERT INTO transactions (id, user_id, type, title, amount, sign, balance_after, timestamp)
                VALUES (:id, :user_id, 'recharge', 'Initial Deposit on Account Creation', :amount, 'positive', :balance_after, :timestamp)
            ")->execute([
                'id' => $txId,
                'user_id' => $userId,
                'amount' => $initialBalance,
                'balance_after' => $initialBalance,
                'timestamp' => $now
            ]);
        }

        return UserModel::findById($userId);
    }

    /**
     * Update User by Admin
     */
    public static function updateUser(string $userId, array $data): array {
        $pdo = Database::getConnection();
        $user = UserModel::findById($userId);
        if (!$user) {
            throw new Exception("User not found.");
        }

        $fullName = $data['fullName'] ?? $data['full_name'] ?? $user['fullName'];
        $email = strtolower($data['email'] ?? $user['email']);
        $phone = $data['phone'] ?? $user['phone'];
        $residentType = $data['residentType'] ?? $data['resident_type'] ?? $user['residentType'];
        $userType = $data['userType'] ?? $data['user_type'] ?? $user['userType'];
        $initials = Utils::calculateInitials($fullName);

        $sql = "UPDATE users SET full_name = :fn, email = :em, phone = :ph, initials = :ini, resident_type = :rt, user_type = :ut, updated_at = NOW()";
        $params = [
            'fn' => $fullName,
            'em' => $email,
            'ph' => $phone,
            'ini' => $initials,
            'rt' => $residentType,
            'ut' => $userType,
            'id' => $userId
        ];

        if (!empty($data['password'])) {
            $sql .= ", password = :pass";
            $params['pass'] = password_hash($data['password'], PASSWORD_DEFAULT);
        }

        $sql .= " WHERE id = :id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);

        return UserModel::findById($userId);
    }

    /**
     * Delete User by Admin
     */
    public static function deleteUser(string $userId): bool {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("DELETE FROM users WHERE id = :id");
        return $stmt->execute(['id' => $userId]);
    }

    /**
     * List Recharge Requests
     */
    public static function getRechargeRequests(?string $status = null): array {
        $pdo = Database::getConnection();
        $sql = "
            SELECT r.*, u.full_name, u.email, u.phone, u.user_type, w.available_balance
            FROM recharge_requests r
            LEFT JOIN users u ON r.user_id = u.id
            LEFT JOIN wallets w ON r.user_id = w.user_id
            WHERE 1=1
        ";
        $params = [];

        if (!empty($status) && strtolower($status) !== 'all') {
            $sql .= " AND r.status = :status";
            $params['status'] = strtolower($status);
        }

        $sql .= " ORDER BY r.created_at DESC";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        return $stmt->fetchAll();
    }

    /**
     * Reject Recharge Request
     */
    public static function rejectRecharge(string $requestId, string $reason = 'Rejected by Admin'): array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("
            UPDATE recharge_requests 
            SET status = 'rejected', admin_note = :note, approved_at = NOW() 
            WHERE id = :id
        ");
        $stmt->execute(['note' => $reason, 'id' => $requestId]);

        return [
            'requestId' => $requestId,
            'status' => 'rejected',
            'adminNote' => $reason
        ];
    }

    /**
     * Delete Notice by Admin
     */
    public static function deleteNotice(string $noticeId): bool {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("DELETE FROM notices WHERE id = :id");
        return $stmt->execute(['id' => $noticeId]);
    }

    /**
     * Mark meal history as consumed / check-in
     */
    public static function markMealConsumed(string $historyId): array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("UPDATE meal_history SET status = 'consumed' WHERE id = :id");
        $stmt->execute(['id' => $historyId]);
        
        $fetch = $pdo->prepare("SELECT * FROM meal_history WHERE id = :id LIMIT 1");
        $fetch->execute(['id' => $historyId]);
        return $fetch->fetch() ?: [];
    }
}
