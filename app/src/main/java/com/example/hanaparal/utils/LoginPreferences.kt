package com.example.hanaparal.utils

import android.content.Context

object LoginPreferences {
    private const val PREF_NAME = "login_prefs"
    private const val KEY_CAN_USE_EMAIL = "can_use_email"

    fun canUseEmailPasswordLogin(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_CAN_USE_EMAIL, false)
    }

    fun setCanUseEmailPasswordLogin(context: Context, canUse: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_CAN_USE_EMAIL, canUse).apply()
    }
}
