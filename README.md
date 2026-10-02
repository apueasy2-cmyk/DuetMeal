# 🍽️ DUET Meal App
> A comprehensive, modern meal management system for Dhaka University of Engineering & Technology (DUET).

DUET Meal is a full-stack application consisting of a sleek **Android App (Kotlin/Jetpack Compose)** and a robust **PHP/MySQL Backend**. It is designed to modernize and simplify canteen management, wallet recharges, and meal booking for students, staff, and canteen administrators.

---

## ✨ Features

### 📱 Android Application (User Facing)
- **Modern UI/UX**: Built entirely with Jetpack Compose using a beautiful Sage Green and Muted Gold brand identity.
- **Digital Wallet**: Real-time balance tracking, quick recharge requests, and secure transaction history.
- **Smart Booking**: One-tap meal booking for lunch and dinner, including guest meal support with cutoff time enforcement.
- **Live Menu**: Check the daily menu for the canteen directly from the app.
- **Notices**: Real-time announcements from the canteen administration.
- **User Profile**: Track your unique ID, email, and personal stats.

### 💻 Web Dashboard (Admin Facing)
- **Comprehensive Analytics**: See today's total recharge requests, meal bookings, bazaar costs, and tomorrow's meal projections.
- **Wallet Management**: Approve or reject digital recharge requests with a single click.
- **Menu & Notice Control**: Broadcast notices to all users and update daily menus effortlessly.
- **Ledger & Settlement**: Daily automated ledger auditing to calculate canteen profit/loss and total cash at hand.
- **Settings Configurator**: Dynamically change meal rates, service availability, cutoff times, and dining charges.

---

## 🛠️ Technology Stack

**Frontend (Android):**
- Kotlin
- Jetpack Compose
- Retrofit (Network Requests)
- Coroutines (Asynchronous Programming)
- Material 3 Design System

**Backend (API & Web Dashboard):**
- PHP 8+ (Vanilla PHP with PDO)
- MySQL Database
- Tailwind CSS (Admin Dashboard Styling)
- Lucide Icons

---

## 🚀 Deployment & Installation

### 1. Backend Setup
The backend is designed to be easily deployed to Apache servers (like XAMPP or AlwaysData) and is fully portable.
1. Upload the `backend with api/` folder to your server's web root (`www` or `htdocs`).
2. Import the `db.sql` file into your MySQL Database.
3. The `config.php` file automatically detects if it is running locally or in production and switches credentials dynamically!

### 2. Android App Setup
1. Open the `android app/` folder in **Android Studio**.
2. Open `ApiConfig.kt` and ensure `BASE_URL` points to your active server IP or domain.
3. Sync Gradle and hit Run!

---

## 🎨 Design System
The app features a custom color palette derived from the legacy DUET brand identity:
- **Brand Primary (Deep Forest Green):** `#226028`
- **Brand Dark:** `#184D1C`
- **Brand Light (Sage Green):** `#A5C49B`
- **Brand Gold (Accent):** `#DFB651`
- **Surface Background:** `#F7FBF6`
