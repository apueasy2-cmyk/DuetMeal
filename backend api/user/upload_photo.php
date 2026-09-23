<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/UserModel.php';

Response::initCors();

$userId = Response::getQueryParam('userId') ?: Utils::getAuthUserId() ?: 'usr_fazlul';

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
$photoUrl = $input['photoUrl'] ?? $input['profilePhoto'] ?? '';
if ($photoUrl) {
    $updated = UserModel::updatePhoto($userId, $photoUrl);
    Response::json($updated);
}

Response::error("No photo file or photoUrl provided.", 400);
