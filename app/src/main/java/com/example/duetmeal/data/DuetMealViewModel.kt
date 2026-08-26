package com.example.duetmeal.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duetmeal.data.model.*
import com.example.duetmeal.network.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Simple sealed class for representing load / error state per call */
sealed class ApiState {
    object Idle : ApiState()
    object Loading : ApiState()
    data class Error(val message: String) : ApiState()
}

/**
 * Shared ViewModel for the entire DuetMeal app.
 * Instantiated once in [MainActivity] and passed to screen composables.
 */
class DuetMealViewModel : ViewModel() {

    private val repository = DuetMealRepository()

    val userId: String get() = ApiConfig.currentUserId

    // ─── State flows ─────────────────────────────────────────────────────────

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _wallet = MutableStateFlow<Wallet?>(null)
    val wallet: StateFlow<Wallet?> = _wallet.asStateFlow()

    private val _todayMeal = MutableStateFlow<TodayMeal?>(null)
    val todayMeal: StateFlow<TodayMeal?> = _todayMeal.asStateFlow()

    private val _historyItems = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyItems: StateFlow<List<HistoryItem>> = _historyItems.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _transactionSummary = MutableStateFlow<TransactionSummary?>(null)
    val transactionSummary: StateFlow<TransactionSummary?> = _transactionSummary.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(emptyList())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _apiState = MutableStateFlow<ApiState>(ApiState.Idle)
    val apiState: StateFlow<ApiState> = _apiState.asStateFlow()

    // ─── Authentication ──────────────────────────────────────────────────────

    fun login(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        _apiState.value = ApiState.Loading
        repository.login(LoginRequest(email.trim(), pass))
            .onSuccess { response ->
                _apiState.value = ApiState.Idle
                val token = response.getEffectiveToken()
                val loggedUser = response.getEffectiveUser()

                if (token != null) {
                    ApiConfig.authToken = token
                }
                if (loggedUser != null) {
                    if (loggedUser.id.isNotEmpty()) {
                        ApiConfig.currentUserId = loggedUser.id
                    }
                    _user.value = loggedUser
                }

                _isLoggedIn.value = true
                loadAll()
                onSuccess()
            }
            .onFailure { error ->
                _apiState.value = ApiState.Error(error.message ?: "Login failed")
                onError(error.message ?: "Invalid email or password. Please try again.")
            }
    }

    fun logout(onSuccess: () -> Unit) = viewModelScope.launch {
        repository.logout()
        ApiConfig.authToken = null
        _isLoggedIn.value = false
        _user.value = null
        _wallet.value = null
        _todayMeal.value = null
        _historyItems.value = emptyList()
        _transactions.value = emptyList()
        _transactionSummary.value = null
        _notices.value = emptyList()
        _bookings.value = emptyList()
        onSuccess()
    }

    // ─── Load All Data ───────────────────────────────────────────────────────

    fun loadAll() {
        loadProfile()
        loadWallet()
        loadTodayMeal()
        loadTransactions("all")
        loadHistory("All")
        loadNotices("All")
    }

    // ─── Profile ─────────────────────────────────────────────────────────────

    fun loadProfile() = viewModelScope.launch {
        repository.getUser(userId).onSuccess { _user.value = it }
    }

    fun updateProfile(
        fullName: String,
        email: String,
        phone: String,
        residentType: String,
        userType: String,
        mealReminder: Boolean,
        autoBooking: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        _apiState.value = ApiState.Loading
        val request = UpdateProfileRequest(
            fullName = fullName,
            email = email,
            phone = phone,
            residentType = residentType,
            userType = userType,
            preferences = UserPreferences(mealReminder, autoBooking)
        )
        repository.updateUser(userId, request)
            .onSuccess {
                _user.value = it
                _apiState.value = ApiState.Idle
                onSuccess()
            }
            .onFailure {
                _apiState.value = ApiState.Error(it.message ?: "Update failed")
                onError(it.message ?: "Update failed")
            }
    }

    // ─── Wallet ──────────────────────────────────────────────────────────────

    fun loadWallet() = viewModelScope.launch {
        repository.getWallet(userId).onSuccess { _wallet.value = it }
    }

    fun requestRecharge(
        amount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        _apiState.value = ApiState.Loading
        repository.requestRecharge(RechargeRequest(userId, amount))
            .onSuccess {
                _apiState.value = ApiState.Idle
                onSuccess()
            }
            .onFailure {
                _apiState.value = ApiState.Error(it.message ?: "Recharge request failed")
                onError(it.message ?: "Recharge request failed")
            }
    }

    // ─── Today's Meal ────────────────────────────────────────────────────────

    fun loadTodayMeal() = viewModelScope.launch {
        repository.getTodayMeal().onSuccess { _todayMeal.value = it }
    }

    // ─── History ─────────────────────────────────────────────────────────────

    /**
     * @param filter "All" | "Consumed" | "Booked" | "Auto-Cancelled"
     */
    fun loadHistory(filter: String) = viewModelScope.launch {
        repository.getHistory(userId, filter).onSuccess { _historyItems.value = it }
    }

    // ─── Transactions ────────────────────────────────────────────────────────

    /**
     * @param type "all" | "recharge" | "meal_deduction" | "settlement"
     */
    fun loadTransactions(type: String) = viewModelScope.launch {
        repository.getTransactions(userId, type).onSuccess { response ->
            _transactions.value = response.transactions
            _transactionSummary.value = response.summary
        }
    }

    // ─── Notices ─────────────────────────────────────────────────────────────

    /**
     * @param category "All" | "dining" | "academic" | "maintenance"
     */
    fun loadNotices(category: String) = viewModelScope.launch {
        repository.getNotices(category).onSuccess { _notices.value = it }
    }

    fun markNoticeRead(noticeId: String) = viewModelScope.launch {
        repository.markNoticeRead(noticeId).onSuccess {
            _notices.value = _notices.value.map { notice ->
                if (notice.id == noticeId) notice.copy(isUnread = false) else notice
            }
        }
    }

    // ─── Bookings ────────────────────────────────────────────────────────────

    fun createBooking(
        request: BookingRequest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) = viewModelScope.launch {
        _apiState.value = ApiState.Loading
        repository.createBooking(request)
            .onSuccess {
                _apiState.value = ApiState.Idle
                loadWallet() // Refresh balance after deduction
                onSuccess()
            }
            .onFailure {
                _apiState.value = ApiState.Error(it.message ?: "Booking failed")
                onError(it.message ?: "Booking failed")
            }
    }
}
