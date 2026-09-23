<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../models/MenuModel.php';

Response::initCors();

$type = Response::getQueryParam('type');
$month = Response::getQueryParam('month');
$date = Response::getQueryParam('date');

if ($month) {
    $menu = MenuModel::getMonthlyMenu($month);
    Response::json($menu);
}

// Default to today's menu
$menu = MenuModel::getTodayMenu($date);
Response::json($menu);
