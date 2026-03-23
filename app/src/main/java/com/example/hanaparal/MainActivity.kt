package com.example.hanaparal

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.ui.theme.*

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                DashboardScreen(
                    viewModel = viewModel,
                    onSuperuserClick = {
                        showBiometricPromptForAdmin()
                    }
                )
            }
        }
    }

    private fun showBiometricPromptForAdmin() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.toggleAdminMode(true)
                    Toast.makeText(this@MainActivity, "Admin Access Granted", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@MainActivity, "Auth Error: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Superuser Verification")
            .setSubtitle("Authenticate to access admin controls")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onSuperuserClick: () -> Unit = {}
) {
    val announcementHeader by viewModel.announcementHeader
    val isGroupCreationEnabled by viewModel.isGroupCreationEnabled
    val isAdminMode by viewModel.isAdminMode
    val maxMembers by viewModel.maxMembersPerGroup
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ── Top Bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HanapAral",
                        style = TextStyle(
                            color = DarkNavy,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = announcementHeader,
                        color = SubtitleGray,
                        fontSize = 14.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSuperuserClick) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.Settings else Icons.Default.Lock,
                            contentDescription = "Admin",
                            tint = if (isAdminMode) Color(0xFF2D8B4E) else SubtitleGray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8ECF0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFFB0B8C4),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ── Search Bar ──
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search study groups...", color = SubtitleGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SubtitleGray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            // ── Admin Controls (Visible only if authenticated) ──
            if (isAdminMode) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Superuser Controls", fontWeight = FontWeight.Bold, color = DarkNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isGroupCreationEnabled,
                                onCheckedChange = { viewModel.updateGroupCreation(it) }
                            )
                            Text("Enable Group Creation", fontSize = 14.sp)
                        }
                        Text("Max Members: $maxMembers", fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                        Button(
                            onClick = { viewModel.toggleAdminMode(false) },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                        ) {
                            Text("Exit Admin", color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Overview ──
            Text(
                text = "Overview",
                color = DarkNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Study Groups",
                    value = "3",
                    icon = Icons.Filled.Person,
                    color = Color(0xFF3B5998)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Subjects",
                    value = "5",
                    icon = Icons.Filled.Star,
                    color = Color(0xFF2D8B4E)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Hours",
                    value = "12",
                    icon = Icons.Filled.DateRange,
                    color = Color(0xFFD4791C)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── My Study Groups ──
            Text(
                text = "My Study Groups",
                color = DarkNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            StudyGroupCard("Data Structures Review", "5 members", "Mon, Wed • 3:00 PM", Color(0xFF3B5998))
            Spacer(modifier = Modifier.height(12.dp))
            StudyGroupCard("Calculus Study Group", "4 members", "Tue, Thu • 10:00 AM", Color(0xFF2D8B4E))
            Spacer(modifier = Modifier.height(12.dp))
            StudyGroupCard("English Literature", "6 members", "Fri • 1:00 PM", Color(0xFFD4791C))

            Spacer(modifier = Modifier.height(28.dp))

            // ── Upcoming Sessions ──
            Text(
                text = "Upcoming Sessions",
                color = DarkNavy,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkNavy
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Data Structures Review",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Today at 3:00 PM • Room 201",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Handle join session */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text("Join Session", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, color = DarkNavy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = SubtitleGray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StudyGroupCard(name: String, members: String, schedule: String, color: Color) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Filled.Person, contentDescription = name, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = DarkNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "$members • $schedule", color = SubtitleGray, fontSize = 12.sp)
            }
        }
    }
}
