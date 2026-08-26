package com.example.duetmeal.data.model

import com.google.gson.annotations.SerializedName

/**
 * Notice posted by admin. Returned by GET /notices
 */
data class Notice(
    val id: String = "",
    /** "dining" | "academic" | "maintenance" */
    val category: String = "dining",
    /** "NEW UPDATE" | "SYSTEM INFO" | "IMPORTANT" */
    val tag: String = "SYSTEM INFO",
    val title: String = "",
    val content: String = "",
    @SerializedName("isUnread")
    val isUnread: Boolean = false,
    val createdAt: String = "",    // ISO8601
    val authorId: String = "admin"
)
