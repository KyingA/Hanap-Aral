package com.example.hanaparal.utils

import android.content.Context

/**
 * When a **notification** message arrives while the app is in the background,
 * [com.google.firebase.messaging.FirebaseMessagingService.onMessageReceived] is **not** called.
 * The in-app list still updates when the user opens the app from a notification tap, because the
 * launcher [android.content.Intent] carries FCM keys — see [FcmMessageParser.maybeIngestFromIntent].
 *
 * (The Flutter/React Native APIs expose `getInitialMessage()`; native Android [com.google.firebase.messaging.FirebaseMessaging]
 * does not — use intent extras here.)
 */
object FcmNotificationBridge {

    fun consumeInitialMessageAndIntent(context: Context, intent: android.content.Intent?) {
        val appCtx = context.applicationContext
        FcmMessageParser.maybeIngestFromIntent(appCtx, intent)
    }
}
