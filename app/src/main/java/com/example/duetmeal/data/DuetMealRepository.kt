package com.example.duetmeal.data

import com.example.duetmeal.data.model.*
import com.example.duetmeal.network.RetrofitClient
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Repository — single data-access layer between the network and the ViewModel.
 * All functions parse API errors to return human-friendly error messages from the server.
 */
class DuetMealRepository {

    private val api = RetrofitClient.api

    private suspend fun <T> runApi(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val parsedMessage = try {
                if (!errorBody.isNullOrEmpty()) {
                    val json = JSONObject(errorBody)
                    json.optString("error", json.optString("message", e.message()))
                } else {
                    e.message()
                }
            } catch (_: Exception) {
                e.message()
            }
            Result.failure(Exception(parsedMessage ?: "Request failed (${e.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: LoginRequest): Result<LoginResponse> =
        runApi { api.login(request) }

    suspend fun logout(): Result<LogoutResponse> =
        runApi { api.logout() }

    suspend fun getMe(): Result<User> =
        runApi { api.getMe() }

    suspend fun getUser(userId: String): Result<User> =
        runApi { api.getUser(userId) }

    suspend fun updateUser(userId: String, request: UpdateProfileRequest): Result<User> =
        runApi { api.updateUser(userId, request) }

    suspend fun getWallet(userId: String): Result<Wallet> =
        runApi { api.getWallet(userId) }

    suspend fun requestRecharge(request: RechargeRequest): Result<RechargeResponse> =
        runApi { api.requestRecharge(request) }

    suspend fun getTodayMeal(): Result<TodayMeal> =
        runApi { api.getTodayMeal() }

    suspend fun getBookings(userId: String, month: String): Result<List<Booking>> =
        runApi { api.getBookings(userId, month) }

    suspend fun createBooking(request: BookingRequest): Result<Booking> =
        runApi { api.createBooking(request) }

    suspend fun cancelBooking(bookingId: String): Result<Unit> =
        runApi { api.cancelBooking(bookingId) }

    suspend fun getHistory(userId: String, filter: String = "All", page: Int = 1): Result<List<HistoryItem>> =
        runApi { api.getHistory(userId, filter, page) }

    suspend fun getTransactions(userId: String, type: String = "all", page: Int = 1): Result<TransactionResponse> =
        runApi { api.getTransactions(userId, type, page) }

    suspend fun getNotices(category: String = "All"): Result<List<Notice>> =
        runApi { api.getNotices(category) }

    suspend fun markNoticeRead(noticeId: String): Result<Unit> =
        runApi { api.markNoticeRead(noticeId) }
}
