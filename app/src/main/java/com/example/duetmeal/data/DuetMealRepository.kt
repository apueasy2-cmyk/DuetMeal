package com.example.duetmeal.data

import com.example.duetmeal.data.model.*
import com.example.duetmeal.network.RetrofitClient

/**
 * Repository — single data-access layer between the network and the ViewModel.
 * All functions use [runCatching] to wrap network errors into [Result].
 */
class DuetMealRepository {

    private val api = RetrofitClient.api

    suspend fun login(request: LoginRequest): Result<LoginResponse> =
        runCatching { api.login(request) }

    suspend fun logout(): Result<LogoutResponse> =
        runCatching { api.logout() }

    suspend fun getMe(): Result<User> =
        runCatching { api.getMe() }

    suspend fun getUser(userId: String): Result<User> =
        runCatching { api.getUser(userId) }

    suspend fun updateUser(userId: String, request: UpdateProfileRequest): Result<User> =
        runCatching { api.updateUser(userId, request) }

    suspend fun getWallet(userId: String): Result<Wallet> =
        runCatching { api.getWallet(userId) }

    suspend fun requestRecharge(request: RechargeRequest): Result<RechargeResponse> =
        runCatching { api.requestRecharge(request) }

    suspend fun getTodayMeal(): Result<TodayMeal> =
        runCatching { api.getTodayMeal() }

    suspend fun getBookings(userId: String, month: String): Result<List<Booking>> =
        runCatching { api.getBookings(userId, month) }

    suspend fun createBooking(request: BookingRequest): Result<Booking> =
        runCatching { api.createBooking(request) }

    suspend fun cancelBooking(bookingId: String): Result<Unit> =
        runCatching { api.cancelBooking(bookingId) }

    suspend fun getHistory(userId: String, filter: String = "All", page: Int = 1): Result<List<HistoryItem>> =
        runCatching { api.getHistory(userId, filter, page) }

    suspend fun getTransactions(userId: String, type: String = "all", page: Int = 1): Result<TransactionResponse> =
        runCatching { api.getTransactions(userId, type, page) }

    suspend fun getNotices(category: String = "All"): Result<List<Notice>> =
        runCatching { api.getNotices(category) }

    suspend fun markNoticeRead(noticeId: String): Result<Unit> =
        runCatching { api.markNoticeRead(noticeId) }
}
