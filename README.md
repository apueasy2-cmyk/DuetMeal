# DUET MEAL - Digital Meal Management System ???

Welcome to **DUET MEAL**, an end-to-end Meal Management System designed for Dhaka University of Engineering & Technology (DUET).

This repository is organized as a monorepo containing both the Android mobile application and the PHP REST API backend.

---

## ?? Repository Structure

`
DUET-MEAL/
+-- ?? android app/       # Jetpack Compose Android Mobile Application
+-- ?? backend api/        # PHP REST API Backend & Admin Panel
`

### 1. ?? Android App (ndroid app/)
Modern Android application built with **Jetpack Compose**, **Material 3**, **Coroutines / StateFlow**, and **Retrofit**.

* **Key Features**:
  * ?? User Authentication & Profile Management
  * ?? Daily Meal Booking & Status Tracking
  * ?? Digital Wallet & Balance Top-up Requests
  * ?? Transaction & Consumed Meal History
  * ?? Campus Notices & Policy Updates
  * ?? In-App Notifications

### 2. ?? Backend API (ackend api/)
PHP REST API server powering the mobile application, featuring an administrative web portal.

* **Key Modules**:
  * user/: Endpoint handlers for Auth, Meal Bookings, Wallet, History, and Notices.
  * dmin/: Admin Control Panel for managing meal rates, user accounts, and deposits.
  * models/: Database Model definitions & SQL query logic.
  * config/: Database connection configuration (Database.php, config.php).
  * db.sql: Database schema & initial seeding script.

---

## ?? Getting Started

### Setting up the Android App
1. Open the [ndroid app](./android%20app) directory in **Android Studio**.
2. Sync Gradle dependencies.
3. Configure your API base URL in ApiConfig.kt or RetrofitClient.kt.
4. Run the project on an Android Emulator or physical device (Min SDK: 24 / Android 7.0+).

### Setting up the Backend API
1. Place the contents of [ackend api](./backend%20api) in your local PHP web server environment (e.g., XAMPP htdocs).
2. Import db.sql into MySQL / MariaDB database (duetmeal).
3. Update database credentials in config/Database.php.
4. Ensure .htaccess / rewrite rules are enabled for clean API endpoints.

---

## ??? Tech Stack

* **Mobile**: Kotlin, Jetpack Compose, Material 3, ViewModel, StateFlow, Retrofit2, OkHttp3, Gson.
* **Backend**: PHP 8.x, MySQL, RESTful API architecture, HTML5/JS Admin Portal.

---

## ?? License
This project is developed for Dhaka University of Engineering & Technology (DUET) Meal Management.
