<?php
require_once __DIR__ . '/../config/Database.php';
require_once __DIR__ . '/../helpers/Utils.php';
require_once __DIR__ . '/WalletModel.php';
require_once __DIR__ . '/SettingModel.php';

class BookingModel {
    public static function getPricePerMeal(): float {
        return (float)SettingModel::get('flat_meal_rate', 90.00);
    }

    public static function createBooking(
        string $userId,
        string $startDate,
        string $endDate,
        bool $includeLunch,
        bool $includeDinner,
        int $guestCount = 0
    ): array {
        // Validation 0: Check if Admin has turned booking service ON / OFF
        $isBookingEnabled = (SettingModel::get('booking_service_enabled', '1') === '1');
        if (!$isBookingEnabled) {
            $serviceMsg = SettingModel::get('booking_service_message', 'Meal booking service is currently closed by Canteen Administration.');
            throw new Exception("Booking Service Closed: " . $serviceMsg);
        }

        if (!$includeLunch && !$includeDinner) {
            throw new Exception("You must select at least Lunch or Dinner for booking.");
        }

        if ($guestCount < 0 || $guestCount > 10) {
            throw new Exception("Guest count must be between 0 and 10.");
        }

        $dates = Utils::getDatesBetween($startDate, $endDate);
        if (empty($dates)) {
            throw new Exception("Invalid date range provided.");
        }

        $now = time();
        $today = date('Y-m-d', $now);
        $currentTime = date('H:i:s', $now);

        // Validation 1: Booking Cutoff for Lunch (12:00 PM same day)
        if ($includeLunch) {
            foreach ($dates as $d) {
                if ($d < $today) {
                    throw new Exception("Cannot book lunch for past date ({$d}).");
                }
                if ($d === $today && $currentTime >= '12:00:00') {
                    throw new Exception("Lunch booking cutoff is 12:00 PM. Cannot book lunch for today after 12:00 PM.");
                }
            }
        }

        // Validation 2: Guest Dinner Policy (Must be confirmed >= 24 hours in advance)
        if ($includeDinner) {
            foreach ($dates as $d) {
                if ($d < $today) {
                    throw new Exception("Cannot book dinner for past date ({$d}).");
                }
                if ($guestCount > 0) {
                    // Dinner starts around 19:30 on the given date
                    $dinnerTimestamp = strtotime("{$d} 19:30:00");
                    $hoursUntilDinner = ($dinnerTimestamp - $now) / 3600;
                    if ($hoursUntilDinner < 24) {
                        throw new Exception("Guest dinner must be booked at least 24 hours in advance ({$d}).");
                    }
                }
            }
        }

        $days = count($dates);
        $mealsPerDay = ($includeLunch ? 1 : 0) + ($includeDinner ? 1 : 0);
        $totalPeople = 1 + $guestCount;
        $pricePerMeal = self::getPricePerMeal();
        $totalCost = (float)($days * $mealsPerDay * $totalPeople * $pricePerMeal);

        // Deduct from wallet immediately
        $title = "Meal Booking (" . ($includeLunch && $includeDinner ? "Lunch & Dinner" : ($includeLunch ? "Lunch" : "Dinner")) . ($guestCount > 0 ? " + {$guestCount} Guest(s)" : "") . ")";
        $bookingId = Utils::generateId('book');

        WalletModel::deductBalance($userId, $totalCost, $title, $bookingId);

        $pdo = Database::getConnection();
        $createdAt = date('Y-m-d H:i:s');

        // Insert booking record
        $stmt = $pdo->prepare("
            INSERT INTO bookings (
                id, user_id, start_date, end_date, include_lunch, include_dinner,
                guest_count, total_people, price_per_meal, total_cost, status, created_at
            ) VALUES (
                :id, :user_id, :start_date, :end_date, :include_lunch, :include_dinner,
                :guest_count, :total_people, :price_per_meal, :total_cost, 'active', :created_at
            )
        ");
        $stmt->execute([
            'id' => $bookingId,
            'user_id' => $userId,
            'start_date' => $startDate,
            'end_date' => $endDate,
            'include_lunch' => $includeLunch ? 1 : 0,
            'include_dinner' => $includeDinner ? 1 : 0,
            'guest_count' => $guestCount,
            'total_people' => $totalPeople,
            'price_per_meal' => $pricePerMeal,
            'total_cost' => $totalCost,
            'created_at' => $createdAt
        ]);

        // Generate individual meal history items for each day
        $participants = ($guestCount > 0) ? "Self + {$guestCount} Guest" . ($guestCount > 1 ? "s" : "") : "Self";
        $costPerIndividualMeal = (float)($totalPeople * $pricePerMeal);

        $histStmt = $pdo->prepare("
            INSERT INTO meal_history (id, user_id, booking_id, date, meal_type, guest_count, participants, status, price, created_at)
            VALUES (:id, :user_id, :booking_id, :date, :meal_type, :guest_count, :participants, 'booked', :price, :created_at)
        ");

        foreach ($dates as $d) {
            if ($includeLunch) {
                $histStmt->execute([
                    'id' => Utils::generateId('hist'),
                    'user_id' => $userId,
                    'booking_id' => $bookingId,
                    'date' => $d,
                    'meal_type' => 'lunch',
                    'guest_count' => $guestCount,
                    'participants' => $participants,
                    'price' => $costPerIndividualMeal,
                    'created_at' => $createdAt
                ]);
            }
            if ($includeDinner) {
                $histStmt->execute([
                    'id' => Utils::generateId('hist'),
                    'user_id' => $userId,
                    'booking_id' => $bookingId,
                    'date' => $d,
                    'meal_type' => 'dinner',
                    'guest_count' => $guestCount,
                    'participants' => $participants,
                    'price' => $costPerIndividualMeal,
                    'created_at' => $createdAt
                ]);
            }
        }

        return self::formatBooking([
            'id' => $bookingId,
            'user_id' => $userId,
            'start_date' => $startDate,
            'end_date' => $endDate,
            'include_lunch' => $includeLunch,
            'include_dinner' => $includeDinner,
            'guest_count' => $guestCount,
            'total_people' => $totalPeople,
            'price_per_meal' => $pricePerMeal,
            'total_cost' => $totalCost,
            'status' => 'active',
            'created_at' => $createdAt
        ]);
    }

    public static function formatBooking(array $row): array {
        return [
            'id' => (string)$row['id'],
            'userId' => (string)$row['user_id'],
            'startDate' => (string)$row['start_date'],
            'endDate' => (string)$row['end_date'],
            'includeLunch' => (bool)$row['include_lunch'],
            'includeDinner' => (bool)$row['include_dinner'],
            'guestCount' => (int)$row['guest_count'],
            'totalPeople' => (int)$row['total_people'],
            'pricePerMeal' => (float)$row['price_per_meal'],
            'totalCost' => (float)$row['total_cost'],
            'status' => (string)$row['status'],
            'createdAt' => (string)$row['created_at']
        ];
    }

    public static function getBookings(?string $userId = null, ?string $month = null): array {
        $pdo = Database::getConnection();
        $sql = "SELECT * FROM bookings WHERE 1=1";
        $params = [];

        if ($userId) {
            $sql .= " AND user_id = :userId";
            $params['userId'] = $userId;
        }

        if ($month) {
            $sql .= " AND (start_date LIKE :monthPrefix OR end_date LIKE :monthPrefix)";
            $params['monthPrefix'] = $month . '%';
        }

        $sql .= " ORDER BY created_at DESC";

        $stmt = $pdo->prepare($sql);
        $stmt->execute($params);
        $rows = $stmt->fetchAll();

        return array_map([self::class, 'formatBooking'], $rows);
    }

    public static function cancelBooking(string $bookingId, ?string $userId = null): array {
        $pdo = Database::getConnection();
        $stmt = $pdo->prepare("SELECT * FROM bookings WHERE id = :id LIMIT 1");
        $stmt->execute(['id' => $bookingId]);
        $booking = $stmt->fetch();

        if (!$booking) {
            throw new Exception("Booking not found.");
        }

        if ($userId && $booking['user_id'] !== $userId) {
            throw new Exception("Unauthorized to cancel this booking.");
        }

        if ($booking['status'] === 'cancelled') {
            throw new Exception("Booking is already cancelled.");
        }

        $today = date('Y-m-d');
        $currentTime = date('H:i:s');

        // Verify cutoff for cancellation
        if ($booking['start_date'] < $today) {
            throw new Exception("Cannot cancel bookings for past dates.");
        }
        if ($booking['start_date'] === $today && $booking['include_lunch'] && $currentTime >= '12:00:00') {
            throw new Exception("Cannot cancel today's lunch after 12:00 PM cutoff.");
        }

        // Mark booking cancelled
        $pdo->prepare("UPDATE bookings SET status = 'cancelled' WHERE id = :id")->execute(['id' => $bookingId]);

        // Mark associated meal history items as auto_cancelled
        $pdo->prepare("UPDATE meal_history SET status = 'auto_cancelled' WHERE booking_id = :id AND status = 'booked'")->execute(['id' => $bookingId]);

        // Refund wallet balance
        $refundAmount = (float)$booking['total_cost'];
        WalletModel::refundBalance(
            $booking['user_id'],
            $refundAmount,
            "Refund for Cancelled Booking #{$bookingId}",
            $bookingId
        );

        $booking['status'] = 'cancelled';
        return self::formatBooking($booking);
    }
}

