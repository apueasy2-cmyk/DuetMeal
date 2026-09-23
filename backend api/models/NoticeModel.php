<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';

class NoticeModel {
    public static function formatNotice(array $row, bool $isUnread = true): array {
        return [
            'id' => (string)$row['id'],
            'category' => strtolower($row['category']),
            'tag' => (string)$row['tag'],
            'title' => (string)$row['title'],
            'content' => (string)$row['content'],
            'isUnread' => $isUnread,
            'createdAt' => (string)$row['created_at'],
            'authorId' => (string)($row['author_id'] ?? 'admin')
        ];
    }

    public static function getNotices(?string $userId = null, ?string $category = null): array {
        $pdo = Database::getConnection();
        $sql = "SELECT * FROM notices WHERE 1=1";
        $params = [];

        if (!empty($category) && strtolower($category) !== 'all') {
            $sql .= " AND LOWER(category) = LOWER(:category)";
            $params['category'] = trim($category);
        }

        $sql .= " ORDER BY created_at DESC";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $notices = $stmt->fetchAll();

        // Get list of read notice IDs for user
        $readNoticeIds = [];
        if ($userId) {
            $readStmt = $pdo->prepare("SELECT notice_id FROM notice_reads WHERE user_id = :userId");
            $readStmt->execute(['userId' => $userId]);
            $readNoticeIds = $readStmt->fetchAll(PDO::FETCH_COLUMN);
        }

        $formatted = [];
        $unreadCount = 0;

        foreach ($notices as $n) {
            $isRead = in_array($n['id'], $readNoticeIds);
            $isUnread = !$isRead;
            if ($isUnread) {
                $unreadCount++;
            }
            $formatted[] = self::formatNotice($n, $isUnread);
        }

        return [
            'unreadCount' => $unreadCount,
            'notices' => $formatted
        ];
    }

    public static function markAsRead(string $noticeId, string $userId): bool {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT COUNT(*) FROM notice_reads WHERE notice_id = :noticeId AND user_id = :userId");
        $stmt->execute(['noticeId' => $noticeId, 'userId' => $userId]);
        if ($stmt->fetchColumn() > 0) {
            return true;
        }

        $insert = $pdo->prepare("
            INSERT INTO notice_reads (id, notice_id, user_id, read_at)
            VALUES (:id, :notice_id, :user_id, :read_at)
        ");
        return $insert->execute([
            'id' => Utils::generateId('nr'),
            'notice_id' => $noticeId,
            'user_id' => $userId,
            'read_at' => date('Y-m-d H:i:s')
        ]);
    }

    public static function createNotice(string $category, string $tag, string $title, string $content, string $authorId = 'admin'): array {
        $pdo = Database::getConnection();
        $id = Utils::generateId('not');
        $createdAt = date('Y-m-d H:i:s');

        $stmt = $pdo->prepare("
            INSERT INTO notices (id, category, tag, title, content, author_id, created_at)
            VALUES (:id, :category, :tag, :title, :content, :author_id, :created_at)
        ");
        $stmt->execute([
            'id' => $id,
            'category' => strtolower($category),
            'tag' => $tag,
            'title' => $title,
            'content' => $content,
            'author_id' => $authorId,
            'created_at' => $createdAt
        ]);

        return [
            'id' => $id,
            'category' => strtolower($category),
            'tag' => $tag,
            'title' => $title,
            'content' => $content,
            'isUnread' => true,
            'createdAt' => $createdAt,
            'authorId' => $authorId
        ];
    }
}
