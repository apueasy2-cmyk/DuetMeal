<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/WalletModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];
$action = Response::getQueryParam('action');

if ($method === 'GET') {
    $userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
    $wallet = WalletModel::getWallet($userId);
    Response::json($wallet);
}

if ($method === 'POST') {
    $input = Response::getJsonInput();
    $subAction = $action ?: ($input['action'] ?? null);

    if ($subAction === 'request') {
        $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
        $amount = (float)($input['amount'] ?? 0);
        $note = $input['note'] ?? null;

        if ($amount <= 0) {
            Response::error("Recharge amount must be greater than zero.", 400);
        }

        $req = WalletModel::requestRecharge($userId, $amount, $note);
        Response::json($req, 201);
    }

    if ($subAction === 'approve') {
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
    }

    Response::error("Unknown wallet action. Use 'request' or 'approve'.", 400);
}

Response::error("Method not allowed", 405);
