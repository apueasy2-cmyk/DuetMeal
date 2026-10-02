package com.example.duetmeal.data.model

/**
 * Request body for POST /bookings — creates a new meal booking
 */
data class BookingRequest(
    val userId: String,
    val startDate: String,       // "YYYY-MM-DD"
    val endDate: String,         // "YYYY-MM-DD"
    val includeLunch: Boolean,
    val includeDinner: Boolean,
    val guestCount: Int = 0,
    val lunchGuestCount: Int = guestCount,
    val dinnerGuestCount: Int = guestCount
)
