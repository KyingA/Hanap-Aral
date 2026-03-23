package com.example.hanaparal.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.theme.*
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("LoginActivity", "Google sign in failed", e)
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                SignInScreen(onGoogleSignInClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                })
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Navigate to MainActivity directly
                    startActivity(Intent(this, com.example.hanaparal.MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}

@Composable
fun SignInScreen(onGoogleSignInClick: () -> Unit = {}) {
    val topGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE8EDF5),
            Color(0xFFD6DEE9),
            Color(0xFFC8D3E2)
        )
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
        ) {
            // ═══════════════════════════════════════
            // ──  TOP HERO SECTION
            // ═══════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(topGradient),
                contentAlignment = Alignment.Center
            ) {
                // Decorative blurred orbs
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .offset(x = (-80).dp, y = (-40).dp)
                        .alpha(0.08f)
                        .blur(90.dp)
                        .background(Color(0xFF3B5998), shape = CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = 140.dp, y = 60.dp)
                        .alpha(0.06f)
                        .blur(90.dp)
                        .background(Color(0xFF1B2B4B), shape = CircleShape)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_book_image),
                        contentDescription = "Book Image",
                        modifier = Modifier.size(200.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "HanapAral",
                        style = TextStyle(
                            color = DarkNavy,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center,
                            shadow = Shadow(
                                color = Color(0xFF1B2B4B).copy(alpha = 0.08f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your Study Companion",
                        style = TextStyle(
                            color = SubtitleGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // ═══════════════════════════════════════
            // ──  BOTTOM CARD SECTION
            // ═══════════════════════════════════════
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 36.dp, bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Heading ──
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = DarkNavy,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            ) {
                                append("Collaborate and\n")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = MediumNavy,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic
                                )
                            ) {
                                append("succeed together.")
                            }
                        },
                        style = TextStyle(
                            fontSize = 30.sp,
                            lineHeight = 38.sp,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Subtitle ──
                    Text(
                        text = "Join curated study groups designed\nfor focused academic excellence.",
                        style = TextStyle(
                            color = SubtitleGray,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            letterSpacing = 0.2.sp,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // ── Google Sign-In Button ──
                    Button(
                        onClick = onGoogleSignInClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavy
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── School Email Link ──
                    Text(
                        text = "Use school email address",
                        color = MediumNavy,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* TODO: Email sign-in */ }
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Divider ──
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFFE0E4EA))
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Footer ──
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = FooterGray)) {
                                append("By continuing, you agree to our ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = FooterGray,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Terms of Service")
                            }
                            withStyle(style = SpanStyle(color = FooterGray)) {
                                append(" and ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = FooterGray,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Privacy Policy")
                            }
                            withStyle(style = SpanStyle(color = FooterGray)) {
                                append(".")
                            }
                        },
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignInScreenPreview() {
    HanapAralTheme(dynamicColor = false) {
        SignInScreen()
    }
}
