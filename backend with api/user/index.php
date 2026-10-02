<?php
// Central API Router for DUET Meal API (/user/...)
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/UserModel.php';
require_once __DIR__ . '/../models/WalletModel.php';
require_once __DIR__ . '/../models/BookingModel.php';
require_once __DIR__ . '/../models/HistoryModel.php';
require_once __DIR__ . '/../models/TransactionModel.php';
require_once __DIR__ . '/../models/NoticeModel.php';
require_once __DIR__ . '/../models/MenuModel.php';
require_once __DIR__ . '/../models/SettlementModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];
$uri = parse_url($_SERVER['REQUEST_URI'], PHP_URL_PATH);

// Normalize path relative to /user
$scriptDir = dirname($_SERVER['SCRIPT_NAME']); // e.g. /duetmealapi/user or /user
$path = substr($uri, strlen($scriptDir));
$path = trim($path, '/');
// Remove .php if present in path
$path = preg_replace('/\.php$/', '', $path);
$segments = $path ? explode('/', $path) : [];

try {
    // -------------------------------------------------------------
    // Route: Root / Documentation / Health
    // -------------------------------------------------------------
    if (empty($segments) || $segments[0] === '') {
        Response::json([
            'status' => 'online',
            'api' => 'DUET Meal Management System API',
            'version' => '1.0.0',
            'currency' => 'BDT (৳)',
            'package' => 'com.example.duetmeal',
            'endpoints' => [
                'POST /user/auth/login',
                'POST /user/auth/logout',
                'GET /user/auth/me',
                'GET /user/users/{userId}',
                'PUT /user/users/{userId}',
                'POST /user/users/{userId}/photo',
                'GET /user/wallet/{userId}',
                'POST /user/wallet/recharge/request',
                'POST /user/wallet/recharge/approve',
                'GET /user/meals/today',
                'GET /user/meals/menu?month=YYYY-MM',
                'POST /user/bookings',
                'GET /user/bookings?userId=&month=',
                'DELETE /user/bookings/{bookingId}',
                'GET /user/history?userId=&filter=&page=',
                'GET /user/transactions?userId=&type=&page=',
                'GET /user/notices?category=',
                'POST /user/notices',
                'PUT /user/notices/{id}/read',
                'POST /user/meal-rate',
                'POST /user/settlement/run'
            ]
        ]);
    }

    // -------------------------------------------------------------
    // Route: /auth/* or /login, /logout, /me
    // -------------------------------------------------------------
    if ($segments[0] === 'auth' || in_array($segments[0], ['login', 'logout', 'me'])) {
        $sub = ($segments[0] === 'auth') ? ($segments[1] ?? '') : $segments[0];

        if ($sub === 'login' && $method === 'POST') {
            $input = Response::getJsonInput();
            $email = $input['email'] ?? '';
            $password = $input['password'] ?? '';

            if (empty($email) || empty($password)) {
                Response::error("Email and password are required.", 400);
            }

            $user = UserModel::authenticate($email, $password);
            if (!$user) {
                Response::error("Invalid email or password.", 401);
            }

            Response::json($user);
        }

        if ($sub === 'logout' && $method === 'POST') {
            $userId = Utils::getAuthUserId();
            if ($userId) {
                UserModel::logout($userId);
            }
            Response::json(['message' => 'Logged out successfully.']);
        }

        if ($sub === 'me' && $method === 'GET') {
            $userId = Utils::getAuthUserId() ?: 'usr_fazlul';
            $user = UserModel::findById($userId);
            if (!$user) {
                Response::error("User not found.", 404);
            }
            Response::json($user);
        }
    }

    // -------------------------------------------------------------
    // Route: /users/{userId} or /profile
    // -------------------------------------------------------------
    if ($segments[0] === 'users' || $segments[0] === 'profile') {
        $userId = $segments[1] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
        $action = $segments[2] ?? null;

        // PUT /users/{userId}/photo or POST /users/{userId}/photo
        if ($action === 'photo' || $segments[0] === 'upload_photo') {
            if (!empty($_FILES['photo']['name'])) {
                $uploadDir = __DIR__ . '/../uploads/profiles/';
                if (!is_dir($uploadDir)) {
                    @mkdir($uploadDir, 0777, true);
                }
                $ext = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
                $filename = 'photo_' . $userId . '_' . time() . '.' . $ext;
                $targetFile = $uploadDir . $filename;

                if (move_uploaded_file($_FILES['photo']['tmp_name'], $targetFile)) {
                    $photoUrl = '/uploads/profiles/' . $filename;
                    $updated = UserModel::updatePhoto($userId, $photoUrl);
                    Response::json($updated);
                } else {
                    Response::error("Failed to upload profile photo.", 500);
                }
            } else {
                $input = Response::getJsonInput();
                $photoUrl = $input['photoUrl'] ?? $input['profilePhoto'] ?? '';
                if ($photoUrl) {
                    $updated = UserModel::updatePhoto($userId, $photoUrl);
                    Response::json($updated);
                }
                Response::error("No photo file or photoUrl provided.", 400);
            }
        }

        if ($method === 'GET') {
            $user = UserModel::findById($userId);
            if (!$user) {
                Response::error("User not found.", 404);
            }
            Response::json($user);
        }

        if ($method === 'PUT' || $method === 'POST') {
            $input = Response::getJsonInput();
            $updated = UserModel::updateProfile($userId, $input);
            if (!$updated) {
                Response::error("User not found or update failed.", 404);
            }
            Response::json($updated);
        }
    }

    // -------------------------------------------------------------
    // Route: /wallet/*
    // -------------------------------------------------------------
    if ($segments[0] === 'wallet') {
        $sub1 = $segments[1] ?? null;
        $sub2 = $segments[2] ?? null;

        // POST /wallet/recharge/request
        if ($sub1 === 'recharge' && $sub2 === 'request' && $method === 'POST') {
            $input = Response::getJsonInput();
            $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
            $amount = (float)($input['amount'] ?? 0);
            $note = $input['note'] ?? null;

            if ($amount <= 0) {
                Response::error("Amount must be greater than zero.", 400);
            }

            $req = WalletModel::requestRecharge($userId, $amount, $note);
            Response::json($req, 201);
        }

        // POST /wallet/recharge/approve
        if ($sub1 === 'recharge' && $sub2 === 'approve' && $method === 'POST') {
            $input = Response::getJsonInput();
            $requestId = $input['requestId'] ?? $input['id'] ?? null;
            $userId = $input['userId'] ?? null;
            $amount = (float)($input['amount'] ?? 0);
            $adminNote = $input['adminNote'] ?? $input['note'] ?? 'Approved by Admin';

            $target = $requestId ?: $userId;
            if (!$target) {
                Response::error("requestId or userId is required for approval.", 400);
            }

            $result = WalletModel::approveRecharge($target, $amount, $adminNote);
            Response::json($result);
        }

        // GET /wallet/{userId}
        if ($method === 'GET') {
            $userId = $sub1 ?: Utils::getAuthUserId() ?: 'usr_fazlul';
            $wallet = WalletModel::getWallet($userId);
            Response::json($wallet);
        }
    }

    // -------------------------------------------------------------
    // Route: /meals/*
    // -------------------------------------------------------------
    if ($segments[0] === 'meals') {
        $sub = $segments[1] ?? 'today';

        // GET /meals/today
        if ($sub === 'today') {
            $date = Response::getQueryParam('date', date('Y-m-d'));
            $menu = MenuModel::getTodayMenu($date);
            Response::json($menu);
        }

        // GET /meals/menu?month=2026-08
        if ($sub === 'menu') {
            $month = Response::getQueryParam('month', date('Y-m'));
            $menu = MenuModel::getMonthlyMenu($month);
            Response::json($menu);
        }
    }

    // -------------------------------------------------------------
    // Route: /bookings/*
    // -------------------------------------------------------------
    if ($segments[0] === 'bookings') {
        $bookingId = $segments[1] ?? null;

        // DELETE /bookings/{bookingId}
        if ($bookingId && ($method === 'DELETE' || ($method === 'POST' && ($segments[2] ?? '') === 'cancel'))) {
            $userId = Utils::getAuthUserId();
            $result = BookingModel::cancelBooking($bookingId, $userId);
            Response::json($result);
        }

        // POST /bookings
        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
            $startDate = $input['startDate'] ?? date('Y-m-d');
            $endDate = $input['endDate'] ?? $startDate;
            $includeLunch = (bool)($input['includeLunch'] ?? false);
            $includeDinner = (bool)($input['includeDinner'] ?? false);
            $guestCount = (int)($input['guestCount'] ?? 0);

            $booking = BookingModel::createBooking($userId, $startDate, $endDate, $includeLunch, $includeDinner, $guestCount);
            Response::json($booking, 201);
        }

        // GET /bookings?userId=&month=
        if ($method === 'GET') {
            $userId = Response::getQueryParam('userId', Utils::getAuthUserId());
            $month = Response::getQueryParam('month');
            $bookings = BookingModel::getBookings($userId, $month);
            Response::json($bookings);
        }
    }

    // -------------------------------------------------------------
    // Route: /history
    // -------------------------------------------------------------
    if ($segments[0] === 'history') {
        $userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
        $filter = Response::getQueryParam('filter', 'All');
        $page = (int)Response::getQueryParam('page', 1);
        $history = HistoryModel::getHistory($userId, $filter, $page);
        Response::json($history);
    }

    // -------------------------------------------------------------
    // Route: /transactions
    // -------------------------------------------------------------
    if ($segments[0] === 'transactions') {
        $userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
        $type = Response::getQueryParam('type', 'all');
        $page = (int)Response::getQueryParam('page', 1);
        $result = TransactionModel::getTransactions($userId, $type, $page);
        Response::json($result);
    }

    // -------------------------------------------------------------
    // Route: /notices/*
    // -------------------------------------------------------------
    if ($segments[0] === 'notices') {
        $noticeId = $segments[1] ?? null;
        $sub = $segments[2] ?? null;

        // PUT /notices/{id}/read or POST /notices/{id}/read
        if ($noticeId && ($sub === 'read' || $method === 'PUT')) {
            $input = Response::getJsonInput();
            $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
            NoticeModel::markAsRead($noticeId, $userId);
            Response::json(['id' => $noticeId, 'userId' => $userId, 'isUnread' => false, 'message' => 'Notice marked as read.']);
        }

        // POST /notices (Create notice)
        if ($method === 'POST') {
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

        // GET /notices?category=
        if ($method === 'GET') {
            $userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
            $category = Response::getQueryParam('category', 'All');
            $result = NoticeModel::getNotices($userId, $category);
            Response::json($result['notices']);
        }
    }

    // -------------------------------------------------------------
    // Route: /meal-rate
    // -------------------------------------------------------------
    if ($segments[0] === 'meal-rate' || $segments[0] === 'meal_rate') {
        if ($method === 'POST') {
            $input = Response::getJsonInput();
            $month = $input['month'] ?? date('Y-m');
            $rate = (float)($input['rate'] ?? 0);
            if ($rate <= 0) {
                Response::error("Meal rate must be greater than zero.", 400);
            }
            $result = SettlementModel::setMealRate($month, $rate);
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

    // Fallback 404
    Response::error("Endpoint not found: {$path}", 404);

} catch (Throwable $e) {
    Response::error($e->getMessage(), 400);
}
