<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Response.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/../models/NoticeModel.php';

Response::initCors();

$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'GET') {
    $userId = Response::getQueryParam('userId', Utils::getAuthUserId() ?: 'usr_fazlul');
    $category = Response::getQueryParam('category', 'All');
    $result = NoticeModel::getNotices($userId, $category);
    Response::json($result['notices']);
}

if ($method === 'PUT' || $method === 'POST') {
    $input = Response::getJsonInput();
    $action = Response::getQueryParam('action') ?: ($input['action'] ?? null);
    $noticeId = Response::getQueryParam('id') ?: ($input['id'] ?? $input['noticeId'] ?? null);

    if ($action === 'read' || ($method === 'PUT' && $noticeId)) {
        if (!$noticeId) {
            Response::error("noticeId is required.", 400);
        }
        $userId = $input['userId'] ?? Utils::getAuthUserId() ?? 'usr_fazlul';
        NoticeModel::markAsRead($noticeId, $userId);
        Response::json(['id' => $noticeId, 'userId' => $userId, 'isUnread' => false, 'message' => 'Notice marked as read.']);
    }

    // Create notice
    $category = $input['category'] ?? 'dining';
    $tag = $input['tag'] ?? 'IMPORTANT';
    $title = $input['title'] ?? '';
    $content = $input['content'] ?? '';
    $authorId = $input['authorId'] ?? 'admin';

    if (empty($title) || empty($content)) {
        Response::error("Title and content are required.", 400);
    }

    $notice = NoticeModel::createNotice($category, $tag, $title, $content, $authorId);
    Response::json($notice, 201);
}

Response::error("Method not allowed", 405);
