package com.example.hanaparal.utils

import android.content.Context

object LoginPreferences {
    private const val PREF_NAME = "login_prefs"
    private const val KEY_CAN_USE_EMAIL = "can_use_email"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    fun canUseEmailPasswordLogin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CAN_USE_EMAIL, false)
    }

    fun setCanUseEmailPasswordLogin(context: Context, canUse: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CAN_USE_EMAIL, canUse).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
}
