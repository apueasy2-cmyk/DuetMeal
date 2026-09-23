package com.example.duetmeal.data.model

import com.google.gson.annotations.SerializedName

/**
 * A single meal service slot (lunch or dinner).
 */
data class MealSlot(
    val venue: String = "Main Canteen",
    @SerializedName("startTime", alternate = ["start_time"])
    val startTime: String = "",
    @SerializedName("endTime", alternate = ["end_time"])
    val endTime: String = "",
    /** Absolute cutoff time for lunch (e.g., "12:00") */
    @SerializedName("cutoffTime", alternate = ["cutoff_time"])
    val cutoffTime: String? = null,
    /** Hours in advance required for guest dinner bookings */
    @SerializedName("cutoffHoursInAdvance", alternate = ["cutoff_hours_in_advance"])
    val cutoffHoursInAdvance: Int? = null,
    val items: List<String> = emptyList(),
    @SerializedName("maxGuests", alternate = ["max_guests"])
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
