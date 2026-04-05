package com.example.hanaparal.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    val hasUnread = _notifications.map { list -> list.any { !it.isRead } }

    fun addNotification(title: String, body: String) {
        val now = System.currentTimeMillis()
        val isDup = _notifications.value.any { existing ->
            existing.title == title && existing.body == body &&
                kotlin.math.abs(now - existing.timestamp) < 15_000L
        }
        if (isDup) return
        val newItem = NotificationItem(title = title, body = body, timestamp = now)
        _notifications.value = listOf(newItem) + _notifications.value
    }

    fun markAsRead(notificationId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(isRead = true) else it
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearNotifications() {
        _notifications.value = emptyList()
    }
}
