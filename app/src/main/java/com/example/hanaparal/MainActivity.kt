package com.example.hanaparal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hanaparal.ui.auth.LoginActivity
import com.example.hanaparal.ui.screens.DashboardScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                DashboardScreen(
                    onCreateClick = {
                        startActivity(Intent(this, CreateGroupActivity::class.java))
                    },
                    onFindGroupClick = {
                        startActivity(Intent(this, GroupListActivity::class.java))
                    },
                    onProfileClick = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    },
                    onGroupClick = { groupId ->
                        val intent = Intent(this, GroupDetailActivity::class.java)
                        intent.putExtra("GROUP_ID", groupId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}
