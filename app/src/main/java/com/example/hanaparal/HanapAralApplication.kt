package com.example.hanaparal

import android.app.Application
import com.google.firebase.FirebaseApp

class HanapAralApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
