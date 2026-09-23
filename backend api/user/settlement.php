<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../models/SettlementModel.php';

Response::initCors();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error("Method not allowed. Use POST.", 405);
}

$input = Response::getJsonInput();
$month = $input['month'] ?? date('Y-m');

try {
    $result = SettlementModel::runSettlement($month);
    Response::json($result);
} catch (Throwable $e) {
    Response::error($e->getMessage(), 400);
}
