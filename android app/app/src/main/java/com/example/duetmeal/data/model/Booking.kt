package com.example.duetmeal.data.model

/**
 * Meal booking as returned by GET /bookings and POST /bookings
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
    val startDate: String = "",           // "YYYY-MM-DD"
    val endDate: String = "",             // "YYYY-MM-DD"
    val includeLunch: Boolean = false,
    val includeDinner: Boolean = false,
    val guestCount: Int = 0,
    val totalPeople: Int = 1,
    val pricePerMeal: Double = 90.0,
    val totalCost: Double = 0.0,
    /** "active" | "cancelled" */
    val status: String = "active",
    val createdAt: String = ""            // ISO8601
)
