<?php
require_once __DIR__ . '/../../../config/Database.php';
require_once __DIR__ . '/../../../helpers/Response.php';
require_once __DIR__ . '/../../../helpers/Utils.php';
require_once __DIR__ . '/../../../models/WalletModel.php';

Response::initCors();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error("Method not allowed. Use POST.", 405);
}

$input = Response::getJsonInput();
$userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
$amount = (float)($input['amount'] ?? 0);
$note = $input['note'] ?? null;

if ($amount <= 0) {
    Response::error("Recharge amount must be greater than zero.", 400);
}

$req = WalletModel::requestRecharge($userId, $amount, $note);
Response::json($req, 201);
