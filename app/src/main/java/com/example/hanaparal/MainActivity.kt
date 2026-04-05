package com.example.hanaparal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.hanaparal.ui.auth.LoginActivity
import com.example.hanaparal.ui.screens.DashboardScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.example.hanaparal.utils.FcmNotificationBridge

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Log.w("FCM", "POST_NOTIFICATIONS denied — tray notifications will not appear on Android 13+")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FcmNotificationBridge.consumeInitialMessageAndIntent(this, intent)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED -> Unit
                else -> notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token ?: "")
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null && token != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Error updating token in Firestore", e)
                        }
                }
            } else {
                Log.w("FCM", "Fetching token failed", task.exception)
            }
        }

        setContent {
            HanapAralTheme(dynamicColor = false) {
                DashboardScreen(mainViewModel = mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        FcmNotificationBridge.consumeInitialMessageAndIntent(this, intent)
    }

    override fun onResume() {
        super.onResume()
        if (::mainViewModel.isInitialized) {
            mainViewModel.refreshFromRemoteConfig()
        }
    }
}
