<div align="center">
  <img src="backend with api/admin/assets/duet_logo.png" alt="DUET Meal Logo" width="120" />

  # 🍽️ DUET Meal App
  **A Next-Generation Canteen Management & Digital Wallet System**

  [![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
  [![Android](https://img.shields.io/badge/Android-Kotlin_Jetpack_Compose-3DDC84?logo=android&logoColor=white)](#)
  [![Backend](https://img.shields.io/badge/Backend-PHP_8.0_MySQL-777BB4?logo=php&logoColor=white)](#)
  
  <p align="center">
    Built for Dhaka University of Engineering & Technology (DUET)
  </p>
</div>

---

## 📖 Overview

The **DUET Meal App** is a comprehensive full-stack ecosystem designed to digitize and streamline dormitory canteen operations. It bridges the gap between canteen administrators and users through a seamless, real-time platform.

This repository contains the complete mono-repo for the project:
1. **The Android Application**: A modern, native Android app built with Kotlin and Jetpack Compose.
2. **The Cloud API & Admin Dashboard**: A robust, lightweight PHP/MySQL backend serving both the RESTful API and a comprehensive web-based management portal.

---

## ✨ Core Capabilities

### 📱 For Users (Android App)
| Feature | Description |
|---------|-------------|
| **💳 Digital Wallet** | Real-time balance tracking, in-app recharge requests, and full transaction ledgers. |
| **📅 Smart Booking** | Automated 1-tap meal booking for Lunch and Dinner. Enforces real-time cutoff policies. |
| **👥 Guest Meals** | Easily add guest meals to your daily booking directly from the dashboard. |
| **📢 Live Notices** | Push-like announcements and dynamic daily menus updated by administrators. |
| **🎨 Premium UI** | A fluid, gesture-driven interface designed with Material 3 in Sage Green & Muted Gold. |

### 🛠️ For Administrators (Web Dashboard)
| Feature | Description |
|---------|-------------|
| **📊 Real-time Analytics** | See today's total revenue, meal counts, bazaar costs, and tomorrow's projections at a glance. |
| **💸 Wallet Management** | One-click approval/rejection of student recharge requests with automated ledger syncing. |
| **📈 Automated Audits** | Automated daily settlement cron jobs that calculate net profit, total costs, and cash at hand. |
| **⚙️ Dynamic Configurator** | Instantly change meal rates, service availability, and operational cutoff times from the cloud. |

---

## 🏗️ System Architecture & Tech Stack

The system is built on a highly portable, decoupled architecture.

- **Mobile Client**: Native Android (Kotlin, Jetpack Compose, Coroutines, Retrofit, ViewModel).
- **RESTful API**: Vanilla PHP 8+ with PDO. Stateless, secure, and lightning-fast.
- **Database**: Relational MySQL with ACID-compliant transactions (InnoDB).
- **Admin Portal**: HTML5, Tailwind CSS, Vanilla JS, Lucide Icons.

---

## 🚀 Getting Started

### 1️⃣ Backend Setup (Local or Cloud)
The backend is portable and runs on any standard Apache server (XAMPP, AlwaysData, cPanel).
1. Clone the repository to your server's web root (`www` or `htdocs`).
2. Import the `backend with api/db.sql` file into your MySQL Database.
3. *Zero Config required:* The `config/config.php` dynamically detects your environment and switches credentials automatically.

### 2️⃣ Android App Setup
1. Open the `android app/` folder in **Android Studio**.
2. Navigate to `ApiConfig.kt` and ensure the `BASE_URL` points to your backend IP or live domain.
3. Sync Gradle and hit **Run**.

---

## 🎨 Brand Identity

The interface utilizes a sophisticated color system derived from the DUET institutional legacy:

- **Deep Forest Green:** `#226028` (Primary)
- **Muted Gold:** `#DFB651` (Accent)
- **Sage Green:** `#A5C49B` (Secondary)
- **Surface Tint:** `#F7FBF6` (Backgrounds)

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
