package com.example.hanaparal.utils

import android.content.Intent
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase / FCM use different keys for title & body depending on message type (notification vs data).
 */
object FcmMessageParser {

    fun titleBodyFromRemoteMessage(remoteMessage: RemoteMessage): Pair<String, String> {
        var title = ""
        var body = ""
        remoteMessage.notification?.let {
            title = it.title.orEmpty()
            body = it.body.orEmpty()
        }
        val data = remoteMessage.data
        if (data.isNotEmpty()) {
            if (title.isEmpty()) {
                title = data["title"]
                    ?: data["gcm.notification.title"]
                    ?: data["subject"]
                    ?: ""
            }
            if (body.isEmpty()) {
                body = data["body"]
                    ?: data["message"]
                    ?: data["gcm.notification.body"]
                    ?: data["alert"]
                    ?: ""
            }
        }
        return title to body
    }

    /**
     * When the user taps a system notification, the opening [Intent] may carry these keys.
     */
    fun titleBodyFromNotificationIntent(extras: Bundle?): Pair<String, String> {
        if (extras == null) return "" to ""
        val title = extras.getString("gcm.notification.title")
            ?: extras.getString("title")
            ?: extras.getString("google.title")
            ?: ""
        val body = extras.getString("gcm.notification.body")
            ?: extras.getString("body")
            ?: extras.getString("message")
            ?: extras.getString("google.body")
            ?: ""
        return title to body
    }

    fun maybeIngestFromIntent(context: android.content.Context, intent: Intent?) {
        if (intent == null) return
        val (t, b) = titleBodyFromNotificationIntent(intent.extras)
        if (t.isNotEmpty() || b.isNotEmpty()) {
            AppNotificationHelper.show(context.applicationContext, t, b)
        }
    }
}
