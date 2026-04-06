package com.example.hanaparal.utils

import android.util.Log
import com.example.hanaparal.data.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title 
            ?: remoteMessage.data["title"] 
            ?: "HanapAral Update"
        val body = remoteMessage.notification?.body 
            ?: remoteMessage.data["body"] 
            ?: ""

        if (title.isNotEmpty() || body.isNotEmpty()) {
            // 1. Always show the tray notification (works offline/background)
            AppNotificationHelper.showTrayOnly(applicationContext, title, body)
            
            // 2. If a user is logged in, also save it to their in-app history
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUserId != null) {
                NotificationRepository.addNotificationForUser(currentUserId, title, body)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnFailureListener { e ->
                    Log.w("FCM", "Error updating refreshed token in Firestore", e)
                }
        }
    }
}
