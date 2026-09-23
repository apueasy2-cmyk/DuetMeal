<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/BookingModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];

try {
    if ($method === 'POST') {
        $input = Response::getJsonInput();
        $action = Response::getQueryParam('action') ?: ($input['action'] ?? null);

        if ($action === 'cancel') {
            $bookingId = $input['bookingId'] ?? $input['id'] ?? Response::getQueryParam('id');
            if (!$bookingId) {
                Response::error("bookingId is required.", 400);
            }
            $userId = Utils::getAuthUserId();
            $result = BookingModel::cancelBooking($bookingId, $userId);
            Response::json($result);
        }

        $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
        $startDate = $input['startDate'] ?? date('Y-m-d');
        $endDate = $input['endDate'] ?? $startDate;
        $includeLunch = (bool)($input['includeLunch'] ?? false);
        $includeDinner = (bool)($input['includeDinner'] ?? false);
        $guestCount = (int)($input['guestCount'] ?? 0);

        $booking = BookingModel::createBooking($userId, $startDate, $endDate, $includeLunch, $includeDinner, $guestCount);
        Response::json($booking, 201);
    }

    if ($method === 'GET') {
        $userId = Response::getQueryParam('userId', Utils::getAuthUserId());
        $month = Response::getQueryParam('month');
        $bookings = BookingModel::getBookings($userId, $month);
        Response::json($bookings);
    }

    if ($method === 'DELETE') {
        $bookingId = Response::getQueryParam('id') ?: Response::getQueryParam('bookingId');
        if (!$bookingId) {
            $input = Response::getJsonInput();
            $bookingId = $input['id'] ?? $input['bookingId'] ?? null;
        }

        if (!$bookingId) {
            Response::error("bookingId is required.", 400);
        }

        $userId = Utils::getAuthUserId();
        $result = BookingModel::cancelBooking($bookingId, $userId);
        Response::json($result);
    }

    Response::error("Method not allowed", 405);
} catch (Throwable $e) {
    Response::error($e->getMessage(), 400);
}
