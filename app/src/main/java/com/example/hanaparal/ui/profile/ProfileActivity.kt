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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.MainActivity
import com.example.hanaparal.ui.theme.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(onComplete: () -> Unit = {}) {
    var fullName by remember { mutableStateOf("") }
    var selectedProgram by remember { mutableStateOf("") }
    var programExpanded by remember { mutableStateOf(false) }

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

            // ── Header ──
            Text(
                text = "HanapAral",
                style = TextStyle(
                    color = DarkNavy,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Title ──
            Text(
                text = "Create your profile",
                style = TextStyle(
                    color = DarkNavy,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Set up your account to get started.",
                color = SubtitleGray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Avatar ──
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8ECF0))
                            .clickable { /* TODO: pick photo */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Add Photo",
                            tint = Color(0xFFB0B8C4),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ADD PHOTO",
                        color = MediumNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Full Name ──
            Text("Full Name", color = DarkNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("e.g. Alex Rivera", color = Color(0xFFB0B8C4)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E4EA),
                    focusedBorderColor = MediumNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                textStyle = TextStyle(color = DarkNavy, fontSize = 15.sp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Course/Program ──
            Text("Course/Program", color = DarkNavy, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = programExpanded,
                onExpandedChange = { programExpanded = !programExpanded }
            ) {
                OutlinedTextField(
                    value = selectedProgram,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select your program", color = Color(0xFFB0B8C4)) },
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, "Expand", tint = SubtitleGray)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E4EA),
                        focusedBorderColor = MediumNavy,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    ),
                    textStyle = TextStyle(color = DarkNavy, fontSize = 15.sp)
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

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // ── Complete Profile Button ──
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Complete Profile",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Continue",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By clicking complete, you agree to our Terms of Service and Privacy Policy.",
                color = FooterGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateProfileScreenPreview() {
    HanapAralTheme(dynamicColor = false) {
        CreateProfileScreen()
    }
}
