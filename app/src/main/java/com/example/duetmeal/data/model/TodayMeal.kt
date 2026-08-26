package com.example.duetmeal.data.model

/**
 * A single meal service slot (lunch or dinner).
 */
data class MealSlot(
    val venue: String = "Main Canteen",
    val startTime: String = "",
    val endTime: String = "",
    /** Absolute cutoff time for lunch (e.g., "12:00") */
    val cutoffTime: String? = null,
    /** Hours in advance required for guest dinner bookings */
    val cutoffHoursInAdvance: Int? = null,
    val items: List<String> = emptyList(),
    val maxGuests: Int = 3
)

/**
 * Today's full meal schedule.
 * Returned by GET /meals/today
 */
data class TodayMeal(
    val date: String = "",   // "YYYY-MM-DD"
    val lunch: MealSlot = MealSlot(),
    val dinner: MealSlot = MealSlot()
)
