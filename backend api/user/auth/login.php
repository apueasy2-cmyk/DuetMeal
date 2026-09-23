<?php
require_once __DIR__ . '/../../config/Database.php';
require_once __DIR__ . '/../../helpers/Response.php';
require_once __DIR__ . '/../../models/UserModel.php';

Response::initCors();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    Response::error("Method not allowed. Use POST.", 405);
}

$input = Response::getJsonInput();
$email = $input['email'] ?? '';
$password = $input['password'] ?? '';

if (empty($email) || empty($password)) {
    Response::error("Email and password are required.", 400);
}

$user = UserModel::authenticate($email, $password);
if (!$user) {
    Response::error("Invalid email or password.", 401);
}

Response::json($user);
