package com.example.duetmeal.data.model

/**
 * Single entry in a user's meal history.
 * Returned by GET /history
 */
data class HistoryItem(
    val id: String = "",
    val userId: String = "",
    val date: String = "",           // "YYYY-MM-DD"
    /** "lunch" | "dinner" */
    val mealType: String = "lunch",
    /** Human-readable, e.g. "Self + 1 Guest" */
    val participants: String = "Self",
    /** "consumed" | "booked" | "auto_cancelled" */
    val status: String = "consumed",
    val price: Double = 0.0
)
