package com.example.hanaparal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hanaparal.ui.screens.DashboardScreen
import com.example.hanaparal.ui.theme.HanapAralTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                DashboardScreen()
            }
        }
    }
}