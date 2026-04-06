package com.example.hanaparal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp

class HanapAralApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "HanapAral Study Group Notifications"
            val descriptionText = "Notifications for group joins, announcements, and reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("hanaparal_notifications", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        lateinit var instance: HanapAralApplication

        val appContext: Context
            get() = instance.applicationContext
    }
}
