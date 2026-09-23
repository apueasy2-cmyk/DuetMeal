<?php

class Response {
    public static function initCors(): void {
        if (!headers_sent()) {
            header("Access-Control-Allow-Origin: *");
            header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
            header("Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With, X-User-Id");
            header("Content-Type: application/json; charset=UTF-8");
        }

        if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
            http_response_code(200);
            exit;
        }
    }

    public static function json($data, int $statusCode = 200): void {
        self::initCors();
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
        exit;
    }

    public static function success($data = null, string $message = 'Success', int $statusCode = 200): void {
        $payload = [
            'success' => true,
            'message' => $message,
        ];
        if ($data !== null) {
            $payload['data'] = $data;
        }
        self::json($payload, $statusCode);
    }

    public static function error(string $message, int $statusCode = 400, $errors = null): void {
        $payload = [
            'success' => false,
            'error' => $message,
            'message' => $message,
        ];
        if ($errors !== null) {
            $payload['errors'] = $errors;
        }
        self::json($payload, $statusCode);
    }

    public static function getJsonInput(): array {
        $raw = file_get_contents('php://input');
        if (!empty($raw)) {
            $decoded = json_decode($raw, true);
            if (is_array($decoded)) {
                return $decoded;
            }
        }
        return $_POST ?? [];
    }

    public static function getQueryParam(string $key, $default = null) {
        return $_GET[$key] ?? $default;
    }
}
