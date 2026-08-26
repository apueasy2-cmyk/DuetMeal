package com.example.duetmeal.network


object ApiConfig {

    // ─── SINGLE SOURCE OF TRUTH FOR THE API URL ──────────────────────────────
    const val BASE_URL = "http://192.168.10.12/duetmealapi/user/"

    var authToken: String? = null

    /** Current logged-in user ID (updated dynamically after login) */
    var currentUserId: String = "1"
}
