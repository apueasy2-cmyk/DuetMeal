package com.example.duetmeal.data.model

import com.google.gson.annotations.SerializedName

data class UserPreferences(
    @SerializedName("mealReminder", alternate = ["meal_reminder"])
    val mealReminder: Boolean = true,
    @SerializedName("autoBooking", alternate = ["auto_booking"])
    val autoBooking: Boolean = false
)

/**
 * User profile as returned by GET /users/{userId} and GET /auth/me
 */
data class User(
    val id: String = "",
    @SerializedName("fullName", alternate = ["full_name", "name"])
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val initials: String = "",
    @SerializedName("residentType", alternate = ["resident_type"])
    val residentType: String = "inside",
    @SerializedName("userType", alternate = ["user_type"])
    val userType: String = "teacher",
    val preferences: UserPreferences = UserPreferences()
)

/** Request body for POST /auth/login */
data class LoginRequest(
    val email: String,
    val password: String
)

/** Inner data container for wrapped login responses */
data class LoginData(
    val token: String? = null,
    val user: User? = null
)

/** Response from POST /auth/login */
data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val token: String? = null,
    val user: User? = null,
    val data: LoginData? = null
) {
    fun getEffectiveToken(): String? = token ?: data?.token
    fun getEffectiveUser(): User? = user ?: data?.user
}

/** Response from POST /auth/logout */
data class LogoutResponse(
    val status: String? = null,
    val message: String? = null
)

/** Request body for PUT /users/{userId} */
data class UpdateProfileRequest(
    @SerializedName("fullName", alternate = ["full_name"])
    val fullName: String,
    val email: String,
    val phone: String,
    @SerializedName("residentType", alternate = ["resident_type"])
    val residentType: String,
    @SerializedName("userType", alternate = ["user_type"])
    val userType: String,
    val preferences: UserPreferences
)
