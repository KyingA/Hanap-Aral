package com.example.hanaparal.data.repository

import android.util.Log
import com.example.hanaparal.HanapAralApplication
import com.example.hanaparal.utils.AppNotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class NotificationItem(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @field:JvmField val isRead: Boolean = false
)

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications = _notifications.asStateFlow()

    val hasUnread = _notifications.map { list -> list.any { !it.isRead } }

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val seenNotificationIds = mutableSetOf<String>()

    /**
     * Starts a real-time listener for the current user's notifications.
     * Automatically triggers a tray notification for any new document added to Firestore.
     */
    fun startListening() {
        val userId = auth.currentUser?.uid ?: return
        Log.d("NotificationRepo", "Started listening for user: $userId")
        
        db.collection("users").document(userId).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("NotificationRepo", "Listen failed. Check Firestore Rules! Error: ${e.message}", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val items = snapshot.toObjects(NotificationItem::class.java)
                    Log.d("NotificationRepo", "Received ${items.size} notifications from Firestore")
                    
                    snapshot.documentChanges.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val item = change.document.toObject(NotificationItem::class.java)
                            if (item.id.isNotEmpty() && !seenNotificationIds.contains(item.id)) {
                                seenNotificationIds.add(item.id)
                                
                                // Increased threshold to 2 minutes to account for sync delays
                                val now = System.currentTimeMillis()
                                val isRecent = (now - item.timestamp) < 120_000 
                                
                                Log.d("NotificationRepo", "New notification detected: ${item.title}. Recent? $isRecent (Diff: ${now - item.timestamp}ms)")
                                
                                if (isRecent) {
                                    AppNotificationHelper.showTrayOnly(
                                        HanapAralApplication.appContext, 
                                        item.title, 
                                        item.body
                                    )
                                }
                            }
                        }
                    }
                    _notifications.value = items
                }
            }
    }

    /**
     * Adds a notification to Firestore. The listener above will handle showing the Tray alert.
     */
    fun addNotificationForUser(userId: String, title: String, body: String) {
        val id = UUID.randomUUID().toString()
        val newItem = NotificationItem(
            id = id,
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        
        Log.d("NotificationRepo", "Saving notification to Firestore for user: $userId")
        db.collection("users").document(userId).collection("notifications")
            .document(id)
            .set(newItem)
            .addOnSuccessListener { Log.d("NotificationRepo", "Notification saved successfully") }
            .addOnFailureListener { Log.e("NotificationRepo", "Failed to save notification. Check Firestore Rules!", it) }
    }

    fun addNotification(title: String, body: String) {
        val userId = auth.currentUser?.uid ?: return
        addNotificationForUser(userId, title, body)
    }

    fun markAsRead(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("notifications").document(notificationId)
            .update("isRead", true)
            .addOnFailureListener { Log.e("NotificationRepo", "Failed to mark notification as read", it) }
    }

    fun markAllAsRead() {
        val userId = auth.currentUser?.uid ?: return
        val unreadItems = _notifications.value.filter { !it.isRead }
        if (unreadItems.isEmpty()) return

        val batch = db.batch()
        unreadItems.forEach { item ->
            val ref = db.collection("users").document(userId).collection("notifications").document(item.id)
            batch.update(ref, "isRead", true)
        }
        batch.commit()
    }
}
