package com.example.hanaparal.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.logic.config.RemoteConfigManager
import com.example.hanaparal.ui.theme.DashboardBg
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.utils.BiometricHelper

class SuperAdminActivity : FragmentActivity() {
    private val remoteConfigManager = RemoteConfigManager()
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biometricHelper = BiometricHelper(this)

        setContent {
            HanapAralTheme(dynamicColor = false) {
                var isAuthorized by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    biometricHelper.showBiometricPrompt(
                        title = "Superuser Access",
                        subtitle = "Authenticate to access admin controls",
                        onSuccess = { isAuthorized = true },
                        onError = { error ->
                            Toast.makeText(this@SuperAdminActivity, "Authentication failed: $error", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }

                if (isAuthorized) {
                    AdminControlScreen(
                        remoteConfigManager = remoteConfigManager,
                        onBack = { finish() }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(DashboardBg), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6366F1))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlScreen(
    remoteConfigManager: RemoteConfigManager,
    onBack: () -> Unit
) {
    var isGroupCreationEnabled by remember { mutableStateOf(remoteConfigManager.isGroupCreationEnabled()) }
    var announcementHeader by remember { mutableStateOf(remoteConfigManager.getAnnouncementHeader()) }
    var maxMembers by remember { mutableStateOf(remoteConfigManager.getMaxGroupMembers().toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Superuser Controls", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardBg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DashboardBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Remote Configuration",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Group Creation Toggle
            AdminSettingCard {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Group Creation", color = Color.White, fontWeight = FontWeight.Medium)
                        Text(
                            "Allow users to create new study groups",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isGroupCreationEnabled,
                        onCheckedChange = { isGroupCreationEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6366F1))
                    )
                }
            }

            // Announcement Header
            AdminSettingCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Announcement Header", color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementHeader,
                        onValueChange = { announcementHeader = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF6366F1)
                        )
                    )
                }
            }

            // Max Members
            AdminSettingCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Default Max Group Members", color = Color.White, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxMembers,
                        onValueChange = { if (it.all { char -> char.isDigit() }) maxMembers = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF6366F1)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // In a real app, you would push these to Firebase Remote Config via an API or use a local override for testing
                    // Since Remote Config is usually read-only from the client, we'll show a Toast
                    // but RemoteConfigManager.kt already provides defaults.
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminSettingCard(content: @Composable () -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        content = content
    )
}
