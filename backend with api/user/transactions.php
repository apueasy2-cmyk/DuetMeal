<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/TransactionModel.php';

Response::initCors();

$userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
$type = Response::getQueryParam('type', 'all');
$page = (int)Response::getQueryParam('page', 1);
$limit = (int)Response::getQueryParam('limit', 20);

$result = TransactionModel::getTransactions($userId, $type, $page, $limit);
Response::json($result);
