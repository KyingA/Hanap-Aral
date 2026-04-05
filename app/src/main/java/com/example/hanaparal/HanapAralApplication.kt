package com.example.hanaparal

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp

class HanapAralApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
    }

    companion object {
        lateinit var instance: HanapAralApplication

        val appContext: Context
            get() = instance.applicationContext
    }
}
