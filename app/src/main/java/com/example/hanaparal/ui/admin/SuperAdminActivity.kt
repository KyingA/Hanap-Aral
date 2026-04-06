package com.example.hanaparal.ui.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.data.config.RemoteConfigManager
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.DashboardScreenBg
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.ui.theme.SubtitleGray
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
                    Box(
                        modifier = Modifier.fillMaxSize().background(DashboardScreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DarkNavy)
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
    val context = LocalContext.current
    var isGroupCreationEnabled by remember { mutableStateOf(remoteConfigManager.isGroupCreationEnabled()) }
    var announcementHeader by remember { mutableStateOf(remoteConfigManager.getAnnouncementHeader()) }
    var maxMembers by remember { mutableStateOf(remoteConfigManager.getMaxGroupMembers().toString()) }

    LaunchedEffect(Unit) {
        remoteConfigManager.fetchAndActivate {
            isGroupCreationEnabled = remoteConfigManager.isGroupCreationEnabled()
            announcementHeader = remoteConfigManager.getAnnouncementHeader()
            maxMembers = remoteConfigManager.getMaxGroupMembers().toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Superuser Controls", fontWeight = FontWeight.Bold, color = DarkNavy)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardScreenBg,
                    titleContentColor = DarkNavy,
                    navigationIconContentColor = DarkNavy
                )
            )
        },
        containerColor = DashboardScreenBg
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
                color = DarkNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Explicit Enable/Disable Section
            AdminSettingCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Group Creation Status", color = DarkNavy, fontWeight = FontWeight.Medium)
                    Text(
                        "Current status: ${if (isGroupCreationEnabled) "ENABLED" else "DISABLED"}",
                        color = if (isGroupCreationEnabled) Color(0xFF4C705B) else Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { isGroupCreationEnabled = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGroupCreationEnabled) Color(0xFF4C705B) else Color(0xFFE5E7EB)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Enable", color = if (isGroupCreationEnabled) Color.White else Color.Gray)
                        }
                        Button(
                            onClick = { isGroupCreationEnabled = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isGroupCreationEnabled) Color(0xFFDC2626) else Color(0xFFE5E7EB)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Block, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Disable", color = if (!isGroupCreationEnabled) Color.White else Color.Gray)
                        }
                    }
                }
            }

            AdminSettingCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Announcement Header", color = DarkNavy, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = announcementHeader,
                        onValueChange = { announcementHeader = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy,
                            cursorColor = DarkNavy,
                            focusedBorderColor = DarkNavy,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6)
                        )
                    )
                }
            }

            AdminSettingCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text("Default Max Group Members", color = DarkNavy, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxMembers,
                        onValueChange = { if (it.all { char -> char.isDigit() }) maxMembers = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy,
                            cursorColor = DarkNavy,
                            focusedBorderColor = DarkNavy,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color(0xFFF3F4F6),
                            unfocusedContainerColor = Color(0xFFF3F4F6)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val cap = maxMembers.toLongOrNull()?.coerceIn(2L, 500L) ?: 10L
                    remoteConfigManager.applyLocalOverrides(
                        groupCreationEnabled = isGroupCreationEnabled,
                        announcementHeader = announcementHeader.trim().ifEmpty { "Welcome to Hanap-Aral!" },
                        maxGroupMembers = cap
                    )
                    Toast.makeText(
                        context,
                        "Configuration applied locally!",
                        Toast.LENGTH_LONG
                    ).show()
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Configuration", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun AdminSettingCard(content: @Composable () -> Unit) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 1.dp,
        content = content
    )
}
