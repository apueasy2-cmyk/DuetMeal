<?php
require_once __DIR__ . '/../../config/Database.php';
require_once __DIR__ . '/../../helpers/Response.php';
require_once __DIR__ . '/../../models/MenuModel.php';

Response::initCors();

$month = Response::getQueryParam('month', date('Y-m'));
$menu = MenuModel::getMonthlyMenu($month);
Response::json($menu);
