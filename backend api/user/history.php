<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/HistoryModel.php';

Response::initCors();

$userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
$filter = Response::getQueryParam('filter', 'All');
$page = (int)Response::getQueryParam('page', 1);
$limit = (int)Response::getQueryParam('limit', 20);

$history = HistoryModel::getHistory($userId, $filter, $page, $limit);
Response::json($history);
