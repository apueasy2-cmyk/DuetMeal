package com.example.duetmeal.network

import com.example.duetmeal.data.model.*
import retrofit2.http.*

/**
 * Retrofit interface defining all DuetMeal API endpoints.
 * Base URL is set in ApiConfig.kt — change it there when IP changes.
 */
interface ApiService {

    // ─── Auth ──────────────────────────────────────────────────────────────────

    /** POST /auth/login — Login with email and password */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    /** POST /auth/logout — Invalidate token and log out */
    @POST("auth/logout")
    suspend fun logout(): LogoutResponse

    /** GET /auth/me — Returns the currently authenticated user profile */
    @GET("auth/me")
    suspend fun getMe(): User

    // ─── Profile ───────────────────────────────────────────────────────────────

    /** GET /users/{userId} — Full user profile */
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): User

    /** PUT /users/{userId} — Update profile fields */
    @PUT("users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequest
    ): User

    // ─── Wallet ────────────────────────────────────────────────────────────────

    /** GET /wallet/{userId} — Wallet balances */
    @GET("wallet/{userId}")
    suspend fun getWallet(@Path("userId") userId: String): Wallet

    /** POST /wallet/recharge/request — User requests a recharge (admin approves) */
    @POST("wallet/recharge/request")
    suspend fun requestRecharge(@Body request: RechargeRequest): RechargeResponse

    // ─── Meals / Menu ──────────────────────────────────────────────────────────

    /** GET /meals/today — Today's lunch & dinner menu */
    @GET("meals/today")
    suspend fun getTodayMeal(): TodayMeal

    // ─── Bookings ──────────────────────────────────────────────────────────────

    /** POST /bookings — Create a new meal booking */
    @POST("bookings")
    suspend fun createBooking(@Body request: BookingRequest): Booking

    /**
     * GET /bookings — User's bookings for a given month
     * @param userId User ID
     * @param month  Month in "YYYY-MM" format (e.g., "2026-08")
     */
    @GET("bookings")
    suspend fun getBookings(
        @Query("userId") userId: String,
        @Query("month") month: String
    ): List<Booking>

    /** DELETE /bookings/{bookingId} — Cancel a booking (before cutoff) */
    @DELETE("bookings/{bookingId}")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String)

    // ─── History ───────────────────────────────────────────────────────────────

    /**
     * GET /history — Paginated meal history
     * @param userId User ID
     * @param filter "All" | "Consumed" | "Booked" | "Auto-Cancelled"
     * @param page   Page number (1-based)
     */
    @GET("history")
    suspend fun getHistory(
        @Query("userId") userId: String,
        @Query("filter") filter: String = "All",
        @Query("page") page: Int = 1
    ): List<HistoryItem>

    // ─── Transactions ──────────────────────────────────────────────────────────

    /**
     * GET /transactions — Transaction list + summary stats
     * @param userId User ID
     * @param type   "all" | "recharge" | "meal_deduction" | "settlement"
     * @param page   Page number (1-based)
     */
    @GET("transactions")
    suspend fun getTransactions(
        @Query("userId") userId: String,
        @Query("type") type: String = "all",
        @Query("page") page: Int = 1
    ): TransactionResponse

    // ─── Notices ───────────────────────────────────────────────────────────────

    /**
     * GET /notices — Notice board entries
     * @param category "All" | "dining" | "academic" | "maintenance"
     */
    @GET("notices")
    suspend fun getNotices(@Query("category") category: String = "All"): List<Notice>

    /** PUT /notices/{id}/read — Mark a notice as read */
    @PUT("notices/{id}/read")
    suspend fun markNoticeRead(@Path("id") noticeId: String)
}
