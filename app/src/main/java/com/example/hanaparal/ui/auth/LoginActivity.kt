package com.example.hanaparal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.hanaparal.MainActivity
import com.example.hanaparal.model.UserProfile
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.screens.SignInScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.utils.LoginPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("LoginActivity", "Google sign in failed", e)
                    Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateAfterAuth()
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.hanaparal.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val canUseEmail = LoginPreferences.canUseEmailPasswordLogin(this)

        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                SignInScreen(
                    canUseEmailPasswordLogin = canUseEmail,
                    onGoogleSignInClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    onRestrictedEmailLoginClick = {
                        Toast.makeText(
                            this,
                            "New accounts must sign in with Google first. After you complete your profile and set a password, you can use email sign-in next time.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onEmailPasswordSignIn = { email, password ->
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    navigateAfterAuth()
                                } else {
                                    val msg = formatFirebaseAuthError(task.exception)
                                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                )
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateAfterAuth()
                } else {
                    Toast.makeText(
                        this,
                        formatFirebaseAuthError(task.exception),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /**
     * Sends users with a completed Firestore profile to the app; others to [ProfileActivity].
     */
    private fun navigateAfterAuth() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Signed in, but user id is missing.", Toast.LENGTH_LONG).show()
            return
        }
        FirebaseFirestore.getInstance().collection("UserProfile").document(uid).get()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    return@addOnCompleteListener
                }
                val doc = task.result
                val profile = doc?.toObject(UserProfile::class.java)
                val hasProfile =
                    doc?.exists() == true && profile?.fullname?.isNotBlank() == true
                if (hasProfile) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                finish()
            }
    }

    private fun formatFirebaseAuthError(e: Exception?): String {
        val fe = e as? FirebaseAuthException
        return when (fe?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "That email address looks invalid."
            "ERROR_WRONG_PASSWORD" -> "Wrong password. Try again."
            "ERROR_INVALID_CREDENTIAL" -> "Wrong email or password."
            "ERROR_USER_NOT_FOUND" -> "No account found for this email. Sign in with Google to create one."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again later."
            else -> fe?.message ?: e?.message ?: "Authentication failed."
        }
    }
}
