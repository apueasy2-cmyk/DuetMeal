package com.example.duetmeal.network


object ApiConfig {

    const val BASE_URL = "https://apudas-server.alwaysdata.net/user/" // AlwaysData Cloud API

    var authToken: String? = null

    /** Current logged-in user ID (updated dynamically after login) */
    var currentUserId: String = "usr_fazlul"
}
