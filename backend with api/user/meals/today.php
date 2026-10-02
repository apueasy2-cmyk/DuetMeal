<?php
require_once __DIR__ . '/../../config/Database.php';
require_once __DIR__ . '/../../helpers/Response.php';
require_once __DIR__ . '/../../models/MenuModel.php';

Response::initCors();

$date = Response::getQueryParam('date');
$menu = MenuModel::getTodayMenu($date);
Response::json($menu);
