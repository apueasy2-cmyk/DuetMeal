<?php
require_once __DIR__ . '/../../config/Database.php';
require_once __DIR__ . '/../../helpers/Response.php';
require_once __DIR__ . '/../../helpers/Utils.php';
require_once __DIR__ . '/../../models/UserModel.php';

Response::initCors();

$userId = Utils::getAuthUserId() ?: 'usr_fazlul';
$user = UserModel::findById($userId);

if (!$user) {
    Response::error("User not found.", 404);
}

Response::json($user);
