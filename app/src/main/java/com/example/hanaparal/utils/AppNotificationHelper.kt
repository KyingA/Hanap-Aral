package com.example.hanaparal.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.hanaparal.MainActivity
import com.example.hanaparal.R
import com.example.hanaparal.data.repository.NotificationRepository

object AppNotificationHelper {

    private const val TAG = "AppNotification"
    private const val CHANNEL_ID = "hanaparal_notifications"

    /**
     * Adds to [NotificationRepository] and shows a system notification.
     */
    fun show(context: Context, title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        val safeTitle = title.ifBlank { "HanapAral" }
        val safeBody = body.ifBlank { "" }
        NotificationRepository.addNotification(safeTitle, safeBody)
        showTrayOnly(context, safeTitle, safeBody)
    }

    /**
     * Shows only the system tray notification without adding to repository.
     */
    fun showTrayOnly(context: Context, title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HanapAral Study Group Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Skipping tray notification: POST_NOTIFICATIONS not granted")
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_book)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify((System.currentTimeMillis() and 0xFFFFFFF).toInt(), notification)
    }
}
