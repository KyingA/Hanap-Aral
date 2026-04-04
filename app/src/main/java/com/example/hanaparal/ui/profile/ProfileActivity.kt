package com.example.hanaparal.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.MainActivity
import com.example.hanaparal.R
import com.example.hanaparal.model.UserProfile
import com.example.hanaparal.ui.theme.*
import com.example.hanaparal.utils.LoginPreferences
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
fun CreateProfileScreen(onComplete: () -> Unit = {}, viewModel: ProfileViewModel =  viewModel()) {
    var fullName by remember { mutableStateOf("") }
    var selectedProgram by remember { mutableStateOf("") }
    var programExpanded by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordFieldError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordFieldError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val profileImages = listOf("beldog", "dopcat", "gencat", "hams", "hartmonks", "monks")
    var selectedImage by remember { mutableStateOf(profileImages[0]) }

    // Preferences state
    var quietStudy by remember { mutableStateOf(true) }
    var peerTeaching by remember { mutableStateOf(false) }
    var projectBased by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val profileError by viewModel.profileError.collectAsState()
    val context = LocalContext.current
    val needsPasswordLink = !viewModel.hasEmailPasswordProvider()

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (it.fullname.isNotBlank()) fullName = it.fullname
            if (it.program.isNotBlank()) selectedProgram = it.program
            if (it.email.isNotBlank()) email = it.email
            quietStudy = it.quietStudy
            peerTeaching = it.peerTeaching
            projectBased = it.projectBased
            if (it.profileImage.isNotBlank() && profileImages.contains(it.profileImage)) {
                selectedImage = it.profileImage
            }
        }
    }

    LaunchedEffect(saveStatus) {
        if (saveStatus == true) {
            LoginPreferences.setCanUseEmailPasswordLogin(context, true)
            viewModel.resetSaveStatus()
            onComplete()
        } else if (saveStatus == false) {
            viewModel.resetSaveStatus()
            val msg = profileError
                ?: "Failed to save profile. Check Firestore Rules or your internet connection."
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.offset(x = (-12).dp)
        ) {
            IconButton(onClick = { 
                val activity = context as? ComponentActivity
                if (activity?.isTaskRoot == true) {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    activity.startActivity(android.content.Intent(activity, com.example.hanaparal.ui.auth.LoginActivity::class.java))
                }
                activity?.finish() 
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DarkNavy
                )
            }
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
            Text(
                text = "CHOOSE AVATAR",
                color = Color(0xFF4C705B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                profileImages.forEach { imageName ->
                    val resourceId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                    if (resourceId != 0) {
                        val isSelected = selectedImage == imageName
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = "Avatar $imageName",
                            modifier = Modifier
                                .size(if (isSelected) 80.dp else 64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) DarkNavy else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedImage = imageName },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
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
            onValueChange = { 
                email = it 
                emailError = false
            },
            placeholder = { Text("king@university.edu", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
            isError = emailError,
            supportingText = { if (emailError) Text("Please enter a valid email address.", color = MaterialTheme.colorScheme.error) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF3F4F6),
                focusedContainerColor = Color(0xFFF3F4F6),
                errorContainerColor = Color(0xFFFEE2E2),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colorScheme.error
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827)),
            singleLine = true
        )

        if (needsPasswordLink) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a password so you can sign in with email next time (uses your Google account email).",
                color = Color(0xFF6B7280),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Password", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordFieldError = null
                    confirmPasswordFieldError = null
                    viewModel.clearProfileError()
                },
                placeholder = { Text("At least 6 characters", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                isError = passwordFieldError != null,
                supportingText = {
                    passwordFieldError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedContainerColor = Color(0xFFF3F4F6),
                    errorContainerColor = Color(0xFFFEE2E2),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827)),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Confirm password", color = Color(0xFF374151), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    passwordFieldError = null
                    confirmPasswordFieldError = null
                    viewModel.clearProfileError()
                },
                placeholder = { Text("Repeat password", color = Color(0xFF9CA3AF), fontSize = 15.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                isError = confirmPasswordFieldError != null,
                supportingText = {
                    confirmPasswordFieldError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF3F4F6),
                    focusedContainerColor = Color(0xFFF3F4F6),
                    errorContainerColor = Color(0xFFFEE2E2),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    errorBorderColor = MaterialTheme.colorScheme.error
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = Color(0xFF111827)),
                singleLine = true
            )
        }

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
                val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                if (!isEmailValid) {
                    emailError = true
                }

                if (fullName.isBlank() || !isEmailValid) return@Button

                val profile = UserProfile(
                    fullname = fullName,
                    program = selectedProgram,
                    email = email,
                    quietStudy = quietStudy,
                    peerTeaching = peerTeaching,
                    projectBased = projectBased,
                    profileImage = selectedImage
                )

                if (viewModel.hasEmailPasswordProvider()) {
                    viewModel.saveUserProfile(profile)
                } else {
                    when {
                        password.length < 6 -> {
                            passwordFieldError = "Password must be at least 6 characters."
                            confirmPasswordFieldError = null
                        }
                        password != confirmPassword -> {
                            val mismatch = "Passwords do not match."
                            passwordFieldError = mismatch
                            confirmPasswordFieldError = mismatch
                        }
                        else -> {
                            passwordFieldError = null
                            confirmPasswordFieldError = null
                            viewModel.completeProfileWithPassword(profile, password)
                        }
                    }
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

