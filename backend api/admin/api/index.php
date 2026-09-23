<?php
// Central Admin API Router for DUET Meal (/admin/api/...)
require_once __DIR__ . '/../../config/Database.php';
require_once __DIR__ . '/../../helpers/Response.php';
require_once __DIR__ . '/../../helpers/Utils.php';
require_once __DIR__ . '/../../models/UserModel.php';
require_once __DIR__ . '/../../models/WalletModel.php';
require_once __DIR__ . '/../../models/BookingModel.php';
require_once __DIR__ . '/../../models/HistoryModel.php';
require_once __DIR__ . '/../../models/TransactionModel.php';
require_once __DIR__ . '/../../models/NoticeModel.php';
require_once __DIR__ . '/../../models/MenuModel.php';
require_once __DIR__ . '/../../models/SettlementModel.php';
require_once __DIR__ . '/../../models/SettingModel.php';
require_once __DIR__ . '/../../models/AdminModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

// Normalize path relative to /admin/api
$scriptDir = dirname($_SERVER['SCRIPT_NAME']); // e.g. /duetmealapi/admin/api or /admin/api
$path = substr($uri, strlen($scriptDir));
$path = trim($path, '/');
$path = preg_replace('/\.php$/', '', $path);
$segments = $path ? explode('/', $path) : [];

try {
    // -------------------------------------------------------------
    // Route: Root / Documentation
    // -------------------------------------------------------------
    if (empty($segments) || $segments[0] === '') {
        Response::json([
            'status' => 'online',
            'api' => 'DUET Meal Admin API',
            'version' => '1.0.0',
            'endpoints' => [
                'GET /admin/api/stats',
                'GET /admin/api/cash?date=YYYY-MM-DD',
                'GET /admin/api/users?search=&userType=&residentType=&page=',
                'POST /admin/api/users',
                'PUT /admin/api/users/{id}',
                'DELETE /admin/api/users/{id}',
                'POST /admin/api/wallet/recharge',
                'GET /admin/api/wallet/requests?status=',
                'POST /admin/api/wallet/requests/{id}/approve',
                'POST /admin/api/wallet/requests/{id}/reject',
                'GET /admin/api/settings',
                'POST /admin/api/settings',
                'POST /admin/api/settings/booking-toggle',
                'POST /admin/api/meal-rate',
                'POST /admin/api/settlement/run',
                'GET /admin/api/menu?date=&month=',
                'POST /admin/api/menu',
                'GET /admin/api/notices',
                'POST /admin/api/notices',
                'DELETE /admin/api/notices/{id}'
            ]
        ]);
    }

    // -------------------------------------------------------------
    // Route: /stats (Dashboard Metrics)
    // -------------------------------------------------------------
    if ($segments[0] === 'stats') {
        $stats = AdminModel::getDashboardStats();
        Response::json($stats);
    }

    // -------------------------------------------------------------
    // Route: /cash (Daily Cash & Transactions Audit)
    // -------------------------------------------------------------
    if ($segments[0] === 'cash') {
        $date = Response::getQueryParam('date', date('Y-m-d'));
        $startDate = Response::getQueryParam('startDate');
        $endDate = Response::getQueryParam('endDate');
        $cashData = AdminModel::getDailyCash($date, $startDate, $endDate);
        Response::json($cashData);
    }

    // -------------------------------------------------------------
    // Route: /users (List, Create, Update, Delete)
    // -------------------------------------------------------------
    if ($segments[0] === 'users') {
        $userId = $segments[1] ?? null;

        // GET /users (List)
        if ($method === 'GET' && !$userId) {
            $search = Response::getQueryParam('search');
            $userType = Response::getQueryParam('userType');
            $residentType = Response::getQueryParam('residentType');
            $page = (int)Response::getQueryParam('page', 1);
            $limit = (int)Response::getQueryParam('limit', 50);

            $result = AdminModel::getAllUsers($search, $userType, $residentType, $page, $limit);
            Response::json($result);
        }

        // GET /users/{id}
        if ($method === 'GET' && $userId) {
            $user = UserModel::findById($userId);
            if (!$user) Response::error("User not found.", 404);
            $wallet = WalletModel::getWallet($userId);
            $user['wallet'] = $wallet;
            Response::json($user);
        }

        // POST /users (Create New User)
        if ($method === 'POST' && !$userId) {
            $input = Response::getJsonInput();
            $user = AdminModel::createUser($input);
            Response::json($user, 201);
        }

        // PUT /users/{id} (Update User)
        if (($method === 'PUT' || $method === 'POST') && $userId) {
            $input = Response::getJsonInput();
            $user = AdminModel::updateUser($userId, $input);
            Response::json($user);
        }

        // DELETE /users/{id}
        if ($method === 'DELETE' && $userId) {
            AdminModel::deleteUser($userId);
            Response::json(['success' => true, 'message' => "User {$userId} deleted successfully."]);
        }
    }

    // -------------------------------------------------------------
    // Route: /wallet (Direct Add Money & Request Approvals)
    // -------------------------------------------------------------
    if ($segments[0] === 'wallet') {
        $sub = $segments[1] ?? null;

        // POST /wallet/recharge (Direct Admin Add Money to User)
        if ($sub === 'recharge' && $method === 'POST') {
            $input = Response::getJsonInput();
            $userId = $input['userId'] ?? null;
            $amount = (float)($input['amount'] ?? 0);
            $adminNote = $input['note'] ?? $input['adminNote'] ?? 'Direct Cash Deposit by Admin';

            if (!$userId) Response::error("userId is required.", 400);
            if ($amount <= 0) Response::error("Amount must be greater than zero.", 400);

            $result = WalletModel::approveRecharge($userId, $amount, $adminNote);
            Response::json($result);
        }

        // GET /wallet/requests (List user recharge requests)
        if ($sub === 'requests' && $method === 'GET') {
            $status = Response::getQueryParam('status', 'all');
            $requests = AdminModel::getRechargeRequests($status);
            Response::json($requests);
        }

        // POST /wallet/requests/{id}/approve
        if ($sub === 'requests' && isset($segments[2]) && ($segments[3] ?? '') === 'approve') {
            $requestId = $segments[2];
            $input = Response::getJsonInput();
            $adminNote = $input['adminNote'] ?? $input['note'] ?? 'Approved by Admin';
            $result = WalletModel::approveRecharge($requestId, 0, $adminNote);
            Response::json($result);
        }

        // POST /wallet/requests/{id}/reject
        if ($sub === 'requests' && isset($segments[2]) && ($segments[3] ?? '') === 'reject') {
            $requestId = $segments[2];
            $input = Response::getJsonInput();
            $reason = $input['adminNote'] ?? $input['reason'] ?? 'Rejected by Admin';
            $result = AdminModel::rejectRecharge($requestId, $reason);
            Response::json($result);
        }
    }

    // -------------------------------------------------------------
    // Route: /settings (Global Config & Booking Service Switch)
    // -------------------------------------------------------------
    if ($segments[0] === 'settings') {
        $sub = $segments[1] ?? null;

        // POST /settings/booking-toggle (Quick Live Switch)
        if ($sub === 'booking-toggle' && $method === 'POST') {
            $input = Response::getJsonInput();
            $enabled = isset($input['enabled']) ? ($input['enabled'] ? '1' : '0') : (SettingModel::get('booking_service_enabled') === '1' ? '0' : '1');
            SettingModel::set('booking_service_enabled', $enabled);
            if (isset($input['message'])) {
                SettingModel::set('booking_service_message', $input['message']);
            }
            Response::json([
                'bookingServiceEnabled' => $enabled === '1',
                'message' => $enabled === '1' ? 'Meal booking service is now ACTIVE.' : 'Meal booking service is now PAUSED.'
            ]);
        }

        // GET /settings
        if ($method === 'GET') {
            $settings = SettingModel::getAll();
            Response::json($settings);
        }

        // POST /settings (Save all system settings)
        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $updated = SettingModel::updateMany($input);
            Response::json($updated);
        }
    }

    // -------------------------------------------------------------
    // Route: /meal-rate (Flat & Monthly Settlement Rates)
    // -------------------------------------------------------------
    if ($segments[0] === 'meal-rate' || $segments[0] === 'meal_rate') {
        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $month = $input['month'] ?? date('Y-m');
            $rate = (float)($input['rate'] ?? 0);
            $isFinalized = isset($input['isFinalized']) ? (bool)$input['isFinalized'] : true;

            if ($rate <= 0) Response::error("Rate must be greater than zero.", 400);

            $result = SettlementModel::setMealRate($month, $rate, $isFinalized);
            Response::json($result);
        }
    }

    // -------------------------------------------------------------
    // Route: /settlement/run
    // -------------------------------------------------------------
    if ($segments[0] === 'settlement') {
        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $month = $input['month'] ?? date('Y-m');
            $result = SettlementModel::runSettlement($month);
            Response::json($result);
        }
    }

    // -------------------------------------------------------------
    // Route: /menu (Today, Monthly, Save Menu)
    // -------------------------------------------------------------
    if ($segments[0] === 'menu') {
        if ($method === 'GET') {
            $date = Response::getQueryParam('date');
            $month = Response::getQueryParam('month');
            if ($month) {
                Response::json(MenuModel::getMonthlyMenu($month));
            } else {
                Response::json(MenuModel::getTodayMenu($date));
            }
        }

        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $date = $input['date'] ?? date('Y-m-d');
            $lunch = $input['lunch'] ?? [];
            $dinner = $input['dinner'] ?? [];

            $saved = MenuModel::saveMenu($date, $lunch, $dinner);
            Response::json($saved);
        }
    }

    // -------------------------------------------------------------
    // Route: /notices (List, Create, Delete)
    // -------------------------------------------------------------
    if ($segments[0] === 'notices') {
        $noticeId = $segments[1] ?? null;

        if ($method === 'GET') {
            $category = Response::getQueryParam('category', 'All');
            $result = NoticeModel::getNotices(null, $category);
            Response::json($result['notices']);
        }

        if ($method === 'POST' && !$noticeId) {
            $input = Response::getJsonInput();
            $category = $input['category'] ?? 'dining';
            $tag = $input['tag'] ?? 'IMPORTANT';
            $title = $input['title'] ?? '';
            $content = $input['content'] ?? '';
            $authorId = $input['authorId'] ?? 'admin';

            if (empty($title) || empty($content)) {
                Response::error("Title and content are required.", 400);
            }

            $notice = NoticeModel::createNotice($category, $tag, $title, $content, $authorId);
            Response::json($notice, 201);
        }

        if ($method === 'DELETE' && $noticeId) {
            AdminModel::deleteNotice($noticeId);
            Response::json(['success' => true, 'message' => "Notice {$noticeId} deleted."]);
        }
    }

    // -------------------------------------------------------------
    // Route: /meals/check-in (Mark as Consumed)
    // -------------------------------------------------------------
    if ($segments[0] === 'meals' && ($segments[1] ?? '') === 'check-in') {
        $input = Response::getJsonInput();
        $historyId = $input['historyId'] ?? $input['id'] ?? null;
        if (!$historyId) Response::error("historyId is required.", 400);
        $result = AdminModel::markMealConsumed($historyId);
        Response::json($result);
    }

    Response::error("Admin API Endpoint not found: {$path}", 404);

} catch (Throwable $e) {
    Response::error($e->getMessage(), 400);
}
