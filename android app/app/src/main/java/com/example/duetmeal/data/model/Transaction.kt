package com.example.duetmeal.data.model

/**
 * A single financial transaction.
 * Returned inside [TransactionResponse] from GET /transactions
 */
data class Transaction(
    val id: String = "",
    val userId: String = "",
    /** "recharge" | "meal_deduction" | "settlement" */
    val type: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    /** "positive" | "negative" */
    val sign: String = "negative",
    val balanceAfter: Double = 0.0,
    val timestamp: String = ""     // ISO8601, e.g. "2026-08-03T08:00:00Z"
)

/** Running totals returned alongside the transaction list */
data class TransactionSummary(
    val totalSpending: Double = 0.0,
    val totalRecharges: Double = 0.0,
    val currentBalance: Double = 0.0
)

/**
 * Wrapper response from GET /transactions
 * Contains the paginated list and summary stats.
 */
data class TransactionResponse(
    val transactions: List<Transaction> = emptyList(),
    val summary: TransactionSummary = TransactionSummary()
)
