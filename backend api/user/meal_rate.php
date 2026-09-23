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
$rate = (float)($input['rate'] ?? 0);

if ($rate <= 0) {
    Response::error("Meal rate must be greater than zero.", 400);
}

$result = SettlementModel::setMealRate($month, $rate);
Response::json($result);
