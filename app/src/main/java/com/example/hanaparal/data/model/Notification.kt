package com.example.hanaparal.data.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // e.g., "JOIN_REQUEST", "ANNOUNCEMENT", "SESSION_REMINDER"
    val groupId: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
