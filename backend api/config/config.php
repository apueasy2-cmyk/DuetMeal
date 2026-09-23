<?php
// Configuration settings for DUET Meal API (MySQL)

return [
    'db' => [
        'host' => 'mysql-apudas-server.alwaysdata.net',
        'port' => 3306,
        'dbname' => 'apudas-server_duetmeal',
        'username' => 'apudas-server',
        'password' => 'P@sssword247',
        'charset' => 'utf8mb4'
    ],
    'app' => [
        'name' => 'DUET Meal API',
        'currency' => 'BDT',
        'currency_symbol' => 'à§³',
        'flat_meal_rate' => 90.00,
        'lunch_cutoff_time' => '12:00:00',
        'dinner_guest_advance_hours' => 24,
        'max_guests' => 10,
        'upload_dir' => __DIR__ . '/../uploads/profiles/',
        'base_url' => '/duetmealapi',
    ]
];
