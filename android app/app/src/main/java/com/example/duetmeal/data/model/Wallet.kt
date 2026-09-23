package com.example.duetmeal.data.model

/**
 * Canteen wallet as returned by GET /wallet/{userId}
 */
data class Wallet(
    val userId: String = "",
    val totalBalance: Double = 0.0,
    val availableBalance: Double = 0.0,
    val currency: String = "BDT"
)

/** Request body for POST /wallet/recharge/request */
data class RechargeRequest(
    val userId: String,
    val amount: Double,
    val note: String = ""
)

/** Response from POST /wallet/recharge/request */
data class RechargeResponse(
    val requestId: String,
    val status: String,
    val message: String
)
