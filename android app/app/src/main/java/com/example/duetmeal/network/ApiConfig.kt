package com.example.duetmeal.network


object ApiConfig {

    // ─── SINGLE SOURCE OF TRUTH FOR THE API URL ──────────────────────────────
    const val BASE_URL = "https://apudas-server.alwaysdata.net/user/"

    var authToken: String? = null

    /** Current logged-in user ID (updated dynamically after login) */
    var currentUserId: String = "usr_fazlul"
}
