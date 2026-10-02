<?php

class Utils {
    public static function generateId(string $prefix = ''): string {
        $uniq = bin2hex(random_bytes(6));
        $time = time();
        return $prefix ? "{$prefix}_{$time}_{$uniq}" : "{$time}_{$uniq}";
    }

    public static function calculateInitials(string $fullName): string {
        $name = trim(preg_replace('/^(Dr\.|Prof\.|Mr\.|Mrs\.|Ms\.|Engr\.)\s+/i', '', trim($fullName)));
        $words = preg_split('/\s+/', $name);
        
        if (empty($words) || empty($words[0])) {
            return 'DM';
        }

        if (count($words) === 1) {
            return strtoupper(substr($words[0], 0, 2));
        }

        $first = strtoupper(substr($words[0], 0, 1));
        $last = strtoupper(substr(end($words), 0, 1));
        return $first . $last;
    }

    public static function getBearerToken(): ?string {
        $headers = function_exists('getallheaders') ? getallheaders() : [];
        $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? $_SERVER['HTTP_AUTHORIZATION'] ?? null;
        
        if ($authHeader && preg_match('/Bearer\s(\S+)/', $authHeader, $matches)) {
            return $matches[1];
        }

        return $_GET['token'] ?? $_POST['token'] ?? null;
    }

    public static function getAuthUserId(): ?string {
        $headers = function_exists('getallheaders') ? getallheaders() : [];
        $userId = $headers['X-User-Id'] ?? $headers['x-user-id'] ?? $_SERVER['HTTP_X_USER_ID'] ?? null;
        if ($userId) {
            return $userId;
        }

        $token = self::getBearerToken();
        if ($token) {
            require_once __DIR__ . '/../config/Database.php';
            $pdo = Database::getConnection();
            $stmt = $pdo->prepare("SELECT id FROM users WHERE auth_token = :token LIMIT 1");
            $stmt->execute(['token' => $token]);
            $found = $stmt->fetchColumn();
            if ($found) {
                return $found;
            }
        }

        return $_GET['userId'] ?? $_POST['userId'] ?? null;
    }

    public static function getDatesBetween(string $startDate, string $endDate): array {
        $dates = [];
        $current = strtotime($startDate);
        $last = strtotime($endDate);

        if ($current === false || $last === false || $current > $last) {
            return [$startDate];
        }

        while ($current <= $last) {
            $dates[] = date('Y-m-d', $current);
            $current = strtotime('+1 day', $current);
        }

        return $dates;
    }
}
