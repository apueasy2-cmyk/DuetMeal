<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/UserModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];
$userId = Response::getQueryParam('userId') ?: Response::getQueryParam('id') ?: Utils::getAuthUserId() ?: 'usr_fazlul';

if ($method === 'GET') {
    $user = UserModel::findById($userId);
    if (!$user) {
        Response::error("User not found.", 404);
    }
    Response::json($user);
}

if ($method === 'POST' || $method === 'PUT') {
    // Check if photo upload
    if (!empty($_FILES['photo']['name'])) {
        $uploadDir = __DIR__ . '/../uploads/profiles/';
        if (!is_dir($uploadDir)) {
            @mkdir($uploadDir, 0777, true);
        }
        $ext = pathinfo($_FILES['photo']['name'], PATHINFO_EXTENSION);
        $filename = 'photo_' . $userId . '_' . time() . '.' . $ext;
        $targetFile = $uploadDir . $filename;

        if (move_uploaded_file($_FILES['photo']['tmp_name'], $targetFile)) {
            $photoUrl = '/uploads/profiles/' . $filename;
            $updated = UserModel::updatePhoto($userId, $photoUrl);
            Response::json($updated);
        } else {
            Response::error("Failed to upload profile photo.", 500);
        }
    }

    $input = Response::getJsonInput();
    $targetUserId = $input['userId'] ?? $input['id'] ?? $userId;
    $updated = UserModel::updateProfile($targetUserId, $input);

    if (!$updated) {
        Response::error("User not found or update failed.", 404);
    }

    Response::json($updated);
}

Response::error("Method not allowed", 405);
