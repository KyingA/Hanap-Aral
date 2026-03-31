package com.example.hanaparal.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.MainActivity
import com.example.hanaparal.R
import com.example.hanaparal.model.UserProfile
import com.example.hanaparal.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                CreateProfileScreen(
                    onComplete = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProfileScreen(onComplete: () -> Unit = {}, viewModel: ProfileViewModel = viewModel()) {
    var fullName by remember { mutableStateOf("") }
    var selectedProgram by remember { mutableStateOf("") }
    var programExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }

    // Preferences state
    var quietStudy by remember { mutableStateOf(true) }
    var peerTeaching by remember { mutableStateOf(false) }
    var projectBased by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(saveStatus) {
        if (saveStatus == true) {
            viewModel.resetSaveStatus()
            onComplete()
        } else if (saveStatus == false) {
            viewModel.resetSaveStatus()
            Toast.makeText(context, "Failed to save profile. Check Firestore Rules or your internet connection.", Toast.LENGTH_LONG).show()
        }
    }

    val programs = listOf(
        "BS Computer Science",
        "BS Information Technology",
        "BS Information Systems",
        "BS Computer Engineering",
        "BS Education",
        "BS Accountancy",
        "BS Business Administration",
        "BS Nursing",
        "BS Psychology",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_book),
                contentDescription = "Logo",
                modifier = Modifier.size(28.dp),
                tint = DarkNavy
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "HanapAral",
                color = DarkNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Create your profile",
            color = DarkNavy,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Join the community of students finding their perfect study groups.",
            color = Color(0xFF6B7280),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Avatar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5E7EB))
                    .clickable { /* TODO: pick photo */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Add photo",
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "ADD PHOTO",
                color = Color(0xFF4C705B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Fields
        Text("Full Name", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = { Text("e.g. King Rivera", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF3F4F6),
                focusedContainerColor = Color(0xFFF3F4F6),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Course/Program", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = programExpanded,
            onExpandedChange = { programExpanded = !programExpanded }
        ) {
            OutlinedTextField(
                value = selectedProgram,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select your program", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
                trailingIcon = {
                    Icon(Icons.Default.KeyboardArrowDown, "Expand", tint = Color(0xFF6B7280))
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedContainerColor = Color(0xFFF3F4F6),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827))
            )
            ExposedDropdownMenu(
                expanded = programExpanded,
                onDismissRequest = { programExpanded = false }
            ) {
                programs.forEach { program ->
                    DropdownMenuItem(
                        text = { Text(program, color = DarkNavy, fontSize = 14.sp) },
                        onClick = {
                            selectedProgram = program
                            programExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Institutional Email", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("king@university.edu", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF3F4F6),
                focusedContainerColor = Color(0xFFF3F4F6),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Preferences section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌿",
                        fontSize = 18.sp,
                        color = Color(0xFF3E2723)
                    )
                    Text(
                        text = "RK",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3E2723),
                        modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                    )
                    Text(
                        text = "Study Preferences",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tell us how you like to study so we can match you better.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PreferenceChip("Quiet Study", selected = quietStudy) { quietStudy = !quietStudy }
                        PreferenceChip("Peer Teaching", selected = peerTeaching) { peerTeaching = !peerTeaching }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PreferenceChip("Project Based", selected = projectBased) { projectBased = !projectBased }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Button
        Button(
            onClick = {
                if (fullName.isNotBlank()) {
                    val profile = UserProfile(
                        fullname = fullName,
                        program = selectedProgram,
                        email = email,
                        quietStudy = quietStudy,
                        peerTeaching = peerTeaching,
                        projectBased = projectBased
                    )
                    viewModel.saveUserProfile(profile)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        "Complete Profile",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                append("By clicking complete, you agree to our ")
                withStyle(style = SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) {
                    append("Terms of\nService")
                }
                append(" and ")
                withStyle(style = SpanStyle(color = DarkNavy, fontWeight = FontWeight.SemiBold)) {
                    append("Privacy Policy")
                }
                append(".")
            },
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PreferenceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFD3EADD) else Color(0xFFF3F4F6)
    val contentColor = if (selected) Color(0xFF4C705B) else Color(0xFF4B5563)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

