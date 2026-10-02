<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/UserModel.php';

Response::initCors();

$userId = Utils::getAuthUserId();
if ($userId) {
    UserModel::logout($userId);
}

Response::json(['message' => 'Logged out successfully.']);
