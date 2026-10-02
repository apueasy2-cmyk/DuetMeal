<?php
require_once __DIR__ . '/../../../config/Database.php';
require_once __DIR__ . '/../../../helpers/Response.php';
require_once __DIR__ . '/../../../models/WalletModel.php';

Response::initCors();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error("Method not allowed. Use POST.", 405);
}

$input = Response::getJsonInput();
$requestId = $input['requestId'] ?? $input['id'] ?? null;
$userId = $input['userId'] ?? null;
$amount = (float)($input['amount'] ?? 0);
$adminNote = $input['adminNote'] ?? $input['note'] ?? 'Approved by Admin';

$target = $requestId ?: $userId;
if (!$target) {
    Response::error("requestId or userId is required.", 400);
}

try {
    $result = WalletModel::approveRecharge($target, $amount, $adminNote);
    Response::json($result);
} catch (Throwable $e) {
    Response::error($e->getMessage(), 400);
}
