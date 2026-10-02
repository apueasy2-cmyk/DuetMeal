# DUET Meal — App Context for API Development

## Overview
**DUET MEAL** is a **Meal Management System** for **DUET (Dhaka University of Engineering & Technology)** canteen/dining facility. It is an Android app (Jetpack Compose, Kotlin) that allows university staff (teachers & officers) to manage their daily canteen meals — booking, payments, history, and notices — through a digital wallet system.

**Package:** `com.example.duetmeal`  
**Tech Stack:** Android + Jetpack Compose + Navigation Compose  
**Currency:** BDT (Bangladeshi Taka — ৳)

---

## User Roles

| Role | Description |
|------|-------------|
| **Teacher** | University teacher, uses DUET canteen |
| **Officer** | University officer/staff, uses DUET canteen |
| **Inside Resident** | Lives inside the university campus |
| **Outside Resident** | Lives outside campus |
| **Admin** | Manages recharges, settlements, and meal rates (backend only) |

---

## Core Domain Concepts

### 1. User / Profile
- **Fields:** Full Name, Email (`@duet.edu.bd`), Phone Number, Profile Photo
- **Type flags:** `isInsideResident` (Inside/Outside), `isOfficer` (Officer/Teacher)
- **Preferences:** `mealReminder` (push notification toggle), `autoBooking` (auto-book meals toggle)
- **Avatar:** Shown as initials (e.g., "FH" for Fazlul Hasan)

---

### 2. Canteen Wallet
- Every user has a **prepaid digital wallet** (Canteen Wallet)
- **Total Balance:** Amount ever deposited (e.g., ৳1,450.00)
- **Available Balance:** Spendable balance after deductions (e.g., ৳1,090.00)
- Balance is **recharged by Admin** (cash recharge reviewed by admin)
- Balance is **auto-deducted** when meals are consumed

---

### 3. Meal Booking
- Users can book **Lunch** and/or **Dinner** in advance
- Meals are booked for a **date range** (start date → end date), for a **selected month**
- Each booking can include **Guest Meals** (0–10 guests, same menu as booker)
- **Price per meal:** ৳90.00 (flat rate, per person per meal)
- **Meal cost formula:** `days × meals_per_day × (1 + guest_count) × ৳90`
- **Cutoff time for Lunch:** 12:00 PM (booking must be done before this)
- **Guest dinner policy:** Must be confirmed ≥24 hours in advance
- On confirm → balance is **deducted immediately**
- Meals can be **cancelled** (before cutoff)
- If not consumed, meal becomes **Auto-Cancelled**

#### Meal Schedule
| Meal | Venue | Time |
|------|-------|------|
| Lunch | Main Canteen | 12:30 PM – 2:00 PM |
| Dinner | Main Canteen | 7:30 PM – 9:00 PM |

---

### 4. Meal History
- Records every meal booking entry per user
- **Status options:** `Consumed`, `Booked`, `Auto-Cancelled`
- Grouped by time period (This Week / Last Week / etc.)
- Each entry: date, meal type (Lunch/Dinner), participants (Self + N Guests), status, total price

---

### 5. Payments & Transactions
Transaction types:
| Type | Description | Sign |
|------|-------------|------|
| `recharge` | Cash recharge added by Admin | `+` |
| `meal_deduction` | Auto-deducted when meal is consumed | `-` |
| `settlement` | Monthly adjustment (rate finalization) | `-` |

- Each transaction has: title, timestamp, balance-after, amount, type
- **Monthly settlement:** At end of each month, actual meal cost is recalculated (e.g., ৳88.10/meal) and adjustments are applied

---

### 6. Notices / Bulletins
- Admin posts notices to all users
- **Categories:** `Dining`, `Academic`, `Maintenance`
- **Types/Tags:** `NEW UPDATE`, `SYSTEM INFO`, `IMPORTANT`
- Each notice has: title, content, timestamp, category, tag, unread indicator (red dot)
- Example notices:
  - *August Meal Rate Finalized* (Dining) — meal rate set to ৳88.10
  - *App Maintenance Notice* (Maintenance) — system downtime
  - *Guest Booking Policy Update* (Dining) — 24h advance required for guest dinner

---

## Navigation / Screens

| Screen | Route | Description |
|--------|-------|-------------|
| **HomeScreen** | `home` | Dashboard: wallet balance, quick service cards, today's meals |
| **BookingScreen** | `booking` | Calendar date-range picker + meal/guest selection + confirm |
| **HistoryScreen** | `history` | Filterable list of past meal bookings |
| **PaymentsScreen** | `payments` | Wallet balance + tabbed transaction history |
| **NoticesScreen** | `notices` | Categorized notice board |
| **ProfileScreen** | `profile` | Edit profile + resident/role toggles + preferences |

Bottom navigation tabs: **Home**, **History**, **Profile** (center floating), **Payments**, **Notice**

---

## API Endpoints Needed (Suggested)

### Auth
- `POST /auth/login` — Login with email + password
- `POST /auth/logout`
- `GET /auth/me` — Get current user profile

### User / Profile
- `GET /users/{userId}` — Get user profile
- `PUT /users/{userId}` — Update profile (name, email, phone, residentType, userType, preferences)
- `PUT /users/{userId}/photo` — Upload profile photo

### Wallet
- `GET /wallet/{userId}` — Get wallet: `totalBalance`, `availableBalance`
- `POST /wallet/recharge/request` — User requests recharge (admin approves)
- `POST /wallet/recharge/approve` — Admin approves recharge (adds to balance)

### Meal Booking
- `GET /meals/today` — Get today's menu (lunch + dinner items, times, cutoff)
- `GET /meals/menu?month=2026-08` — Get monthly menu
- `POST /bookings` — Create booking (body: userId, startDate, endDate, includeLunch, includeDinner, guestCount)
- `GET /bookings?userId=&month=` — Get user's bookings for a month
- `DELETE /bookings/{bookingId}` — Cancel a booking (before cutoff)

### History
- `GET /history?userId=&filter=&page=` — Get meal history (filter: All/Consumed/Booked/Auto-Cancelled)

### Transactions
- `GET /transactions?userId=&type=&page=` — Get transaction list (type: all/recharge/deduction/settlement)
- Summary stats: totalSpending, totalRecharges, currentBalance

### Notices
- `GET /notices?category=` — Get notices (category: All/Dining/Academic/Maintenance)
- `POST /notices` — Admin: create a notice
- `PUT /notices/{id}/read` — Mark notice as read

### Admin (optional scope)
- `POST /meal-rate` — Set monthly meal rate
- `POST /settlement/run` — Trigger monthly balance settlement

---

## Data Models (JSON shape)

### User
```json
{
  "id": "string",
  "fullName": "Dr. Fazlul Hasan",
  "email": "fazlul.hasan@duet.edu.bd",
  "phone": "+880 1234 567890",
  "initials": "FH",
  "residentType": "inside | outside",
  "userType": "officer | teacher",
  "preferences": {
    "mealReminder": true,
    "autoBooking": false
  }
}
```

### Wallet
```json
{
  "userId": "string",
  "totalBalance": 1450.00,
  "availableBalance": 1090.00,
  "currency": "BDT"
}
```

### Booking
```json
{
  "id": "string",
  "userId": "string",
  "startDate": "2026-08-10",
  "endDate": "2026-08-14",
  "includeLunch": true,
  "includeDinner": true,
  "guestCount": 1,
  "totalPeople": 2,
  "pricePerMeal": 90.00,
  "totalCost": 900.00,
  "status": "active | cancelled",
  "createdAt": "ISO8601"
}
```

### Meal History Item
```json
{
  "id": "string",
  "userId": "string",
  "date": "2026-08-10",
  "mealType": "lunch | dinner",
  "participants": "Self + 1 Guest",
  "status": "consumed | booked | auto_cancelled",
  "price": 180.00
}
```

### Transaction
```json
{
  "id": "string",
  "userId": "string",
  "type": "recharge | meal_deduction | settlement",
  "title": "Cash Recharge by Admin",
  "amount": 2000.00,
  "sign": "positive | negative",
  "balanceAfter": 3360.00,
  "timestamp": "2026-08-03T08:00:00Z"
}
```

### Notice
```json
{
  "id": "string",
  "category": "dining | academic | maintenance",
  "tag": "NEW UPDATE | SYSTEM INFO | IMPORTANT",
  "title": "August Meal Rate Finalized",
  "content": "The meal rate for August 2026 has been calculated at ৳88.10...",
  "isUnread": true,
  "createdAt": "ISO8601",
  "authorId": "admin"
}
```

### Today's Meal (Menu)
```json
{
  "date": "2026-05-24",
  "lunch": {
    "venue": "Main Canteen",
    "startTime": "12:30",
    "endTime": "14:00",
    "cutoffTime": "12:00",
    "items": ["Chicken Biryani", "Salad", "Borhani"],
    "maxGuests": 3
  },
  "dinner": {
    "venue": "Main Canteen",
    "startTime": "19:30",
    "endTime": "21:00",
    "cutoffHoursInAdvance": 24,
    "items": ["Steamed Rice", "Fish Curry", "Dal"],
    "maxGuests": 3
  }
}
```

---

## Business Rules (Important for API)
1. **Booking cutoff:** Lunch must be booked before 12:00 PM same day
2. **Guest dinner:** Must be booked ≥24 hours in advance
3. **Max guests per meal:** Up to 10 (UI allows 0–10)
4. **Meal price:** ৳90.00/person/meal (flat, before monthly settlement)
5. **Monthly settlement:** Actual rate may differ (e.g., ৳88.10); adjustment is made end-of-month
6. **Recharge:** User requests, Admin approves — no self-recharge
7. **Auto-cancel:** If booked but not consumed (no check-in/scan), status becomes `auto_cancelled`
8. **Balance deduction:** Happens at booking confirm time (pre-paid model)
9. **Notice unread count:** Badge on bottom nav shows count of unread notices (currently 3)
10. **User initials:** Derived from name (first letters of first + last name)
