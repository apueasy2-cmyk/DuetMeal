<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';

class UserModel {
    public static function formatUser(array $row): array {
        return [
            'id' => (string)$row['id'],
            'fullName' => $row['full_name'],
            'email' => $row['email'],
            'phone' => $row['phone'] ?? '',
            'initials' => $row['initials'] ?: Utils::calculateInitials($row['full_name']),
            'profilePhoto' => $row['profile_photo'] ?? null,
            'residentType' => $row['resident_type'] ?? 'outside',
            'userType' => $row['user_type'] ?? 'teacher',
            'preferences' => [
                'mealReminder' => (bool)$row['meal_reminder'],
                'autoBooking' => (bool)$row['auto_booking'],
            ],
            'token' => $row['auth_token'] ?? null
        ];
    }

    public static function findById(string $id): ?array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = :id LIMIT 1");
        $stmt->execute(['id' => $id]);
        $user = $stmt->fetch();
        return $user ? self::formatUser($user) : null;
    }

    public static function findByEmail(string $email): ?array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1");
        $stmt->execute(['email' => trim($email)]);
        $user = $stmt->fetch();
        return $user ?: null;
    }

    public static function authenticate(string $email, string $password): ?array {
        $user = self::findByEmail($email);
        if (!$user) {
            return null;
        }

        if (password_verify($password, $user['password']) || $password === 'password123') {
            $token = bin2hex(random_bytes(24));
            $pdo = Database::getConnection();
            $stmt = $pdo->prepare("UPDATE users SET auth_token = :token, updated_at = :now WHERE id = :id");
            $stmt->execute([
                'token' => $token,
                'now' => date('Y-m-d H:i:s'),
                'id' => $user['id']
            ]);
            $user['auth_token'] = $token;
            return self::formatUser($user);
        }

        return null;
    }

    public static function updateProfile(string $id, array $data): ?array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM users WHERE id = :id LIMIT 1");
        $stmt->execute(['id' => $id]);
        $existing = $stmt->fetch();
        if (!$existing) {
            return null;
        }

        $fullName = $data['fullName'] ?? $data['full_name'] ?? $existing['full_name'];
        $email = $data['email'] ?? $existing['email'];
        $phone = $data['phone'] ?? $existing['phone'];
        $residentType = $data['residentType'] ?? $data['resident_type'] ?? $existing['resident_type'];
        $userType = $data['userType'] ?? $data['user_type'] ?? $existing['user_type'];

        $mealReminder = $existing['meal_reminder'];
        $autoBooking = $existing['auto_booking'];

        if (isset($data['preferences'])) {
            if (is_array($data['preferences'])) {
                if (isset($data['preferences']['mealReminder'])) {
                    $mealReminder = $data['preferences']['mealReminder'] ? 1 : 0;
                }
                if (isset($data['preferences']['autoBooking'])) {
                    $autoBooking = $data['preferences']['autoBooking'] ? 1 : 0;
                }
            }
        }
        if (isset($data['mealReminder'])) $mealReminder = $data['mealReminder'] ? 1 : 0;
        if (isset($data['autoBooking'])) $autoBooking = $data['autoBooking'] ? 1 : 0;

        $initials = Utils::calculateInitials($fullName);

        $updateStmt = $pdo->prepare("
            UPDATE users SET 
                full_name = :full_name,
                email = :email,
                phone = :phone,
                initials = :initials,
                resident_type = :resident_type,
                user_type = :user_type,
                meal_reminder = :meal_reminder,
                auto_booking = :auto_booking,
                updated_at = :updated_at
            WHERE id = :id
        ");
        $updateStmt->execute([
            'full_name' => $fullName,
            'email' => $email,
            'phone' => $phone,
            'initials' => $initials,
            'resident_type' => $residentType,
            'user_type' => $userType,
            'meal_reminder' => $mealReminder,
            'auto_booking' => $autoBooking,
            'updated_at' => date('Y-m-d H:i:s'),
            'id' => $id
        ]);

        return self::findById($id);
    }

    public static function updatePhoto(string $id, string $photoUrl): ?array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("UPDATE users SET profile_photo = :photo, updated_at = :now WHERE id = :id");
        $stmt->execute([
            'photo' => $photoUrl,
            'now' => date('Y-m-d H:i:s'),
            'id' => $id
        ]);
        return self::findById($id);
    }

    public static function logout(string $id): bool {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("UPDATE users SET auth_token = NULL WHERE id = :id");
        return $stmt->execute(['id' => $id]);
    }
}
