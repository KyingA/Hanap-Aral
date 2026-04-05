package com.example.hanaparal.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.DashboardScreenBg
import com.example.hanaparal.ui.theme.SubtitleGray

@Composable
fun SignInScreen(
    canUseEmailPasswordLogin: Boolean,
    onGoogleSignInClick: () -> Unit,
    onRestrictedEmailLoginClick: () -> Unit,
    onEmailPasswordSignIn: (String, String) -> Unit
) {
    // We only display the visual from the design mockup.
    // We optionally add the email logic via a pop-up or secondary button if allowed, 
    // but the design emphasizes Google Login.

    var showEmailDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bookLogoRes = remember(context) {
        val id = context.resources.getIdentifier("book", "drawable", context.packageName)
        if (id != 0) id else R.drawable.ic_book
    }

    if (showEmailDialog) {
        EmailLoginDialog(
            onDismiss = { showEmailDialog = false },
            onSignIn = onEmailPasswordSignIn
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardScreenBg)
    ) {
        // Upper background section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = bookLogoRes),
                contentDescription = "Book Logo",
                modifier = Modifier
                    .size(200.dp)
                    .offset(y = (-20).dp)
            )

            Text(
                text = "HanapAral",
                color = DarkNavy,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your Study Companion",
                color = SubtitleGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
        }

        // Bottom card sheet
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Collaborate and\nsucceed together.",
                    color = DarkNavy,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Join curated study groups designed\nfor focused academic excellence.",
                    color = SubtitleGray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onGoogleSignInClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                var showRestrictedAlert by remember { mutableStateOf(false) }
                
                if (showRestrictedAlert) {
                    AlertDialog(
                        onDismissRequest = { showRestrictedAlert = false },
                        title = { Text("Use Google Sign-In First", fontWeight = FontWeight.Bold) },
                        text = { Text("For security and profile setup, new accounts must sign in with Google. Once you complete your profile, you will set a password and can use email sign-in seamlessly next time!") },
                        confirmButton = {
                            Button(onClick = { showRestrictedAlert = false }) {
                                Text("Got it")
                            }
                        }
                    )
                }

                if (canUseEmailPasswordLogin) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showEmailDialog = true }) {
                        Text("Sign in with Email instead", color = DarkNavy, fontSize = 14.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { showRestrictedAlert = true }) {
                        Text("Sign in with Email instead", color = DarkNavy, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(
                    color = Color(0xFFE5E7EB),
                    thickness = 2.dp,
                    modifier = Modifier.width(40.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "By continuing, you agree to our Terms of Service and Privacy Policy.",
                    color = Color(0xFF9CA3AF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun EmailLoginDialog(
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign In with Email",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter your credentials to continue",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password", color = Color.Gray) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = DarkNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkNavy,
                        unfocusedBorderColor = Color(0xFFE5E7EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB)
                    )
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        onSignIn(email, password)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                ) {
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
