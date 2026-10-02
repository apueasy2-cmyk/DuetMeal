<?php
// Configuration settings for DUET Meal API (MySQL)

$isProduction = isset($_SERVER['HTTP_HOST']) && strpos($_SERVER['HTTP_HOST'], 'alwaysdata.net') !== false;

return [
    'db' => [
        'host' => $isProduction ? 'mysql-apudas-server.alwaysdata.net' : '127.0.0.1',
        'port' => 3306,
        'dbname' => $isProduction ? 'apudas-server_duetmeal' : 'duetmeal',
        'username' => $isProduction ? 'apudas-server' : 'root',
        'password' => $isProduction ? 'P@sssword247' : '',
        'charset' => 'utf8mb4'
    ],
    'app' => [
        'name' => 'DUET Meal API',
        'currency' => 'BDT',
        'currency_symbol' => '৳',
        'flat_meal_rate' => 90.00,
        'lunch_cutoff_time' => '12:00:00',
        'dinner_guest_advance_hours' => 24,
        'max_guests' => 10,
        'upload_dir' => __DIR__ . '/../uploads/profiles/',
        'base_url' => '/duetmealapi',
    ]
];
