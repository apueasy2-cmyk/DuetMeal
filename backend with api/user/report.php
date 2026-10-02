<?php
// POST /user/report.php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';

Response::initCors();

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $userId = $data['userId'] ?? null;
    $message = $data['message'] ?? null;
    $logs = $data['logs'] ?? null;

    if (!$userId || !$message) {
        Response::error('User ID and message are required', 400);
    }

    $id = Utils::generateId('rep');
    $pdo = Database::getConnection();
    
    $stmt = $pdo->prepare("
        INSERT INTO reports (id, user_id, message, logs, status, created_at) 
        VALUES (:id, :uid, :msg, :logs, 'pending', NOW())
    ");
    $stmt->execute([
        'id' => $id,
        'uid' => $userId,
        'msg' => $message,
        'logs' => $logs
    ]);

    Response::json(['message' => 'Report submitted successfully.', 'id' => $id]);
}

Response::error('Method not allowed', 405);
