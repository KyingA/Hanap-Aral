package com.example.hanaparal.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.example.hanaparal.MainActivity
import com.example.hanaparal.R
import com.example.hanaparal.data.model.UserProfile
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.screens.SignInScreen
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.utils.LoginPreferences
import com.example.hanaparal.utils.BiometricHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.hanaparal.utils.FcmNotificationBridge

class LoginActivity : FragmentActivity() {

    companion object {
        const val EXTRA_AFTER_LOGOUT = "com.example.hanaparal.EXTRA_AFTER_LOGOUT"
    }

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
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FcmNotificationBridge.consumeInitialMessageAndIntent(this, intent)
        auth = FirebaseAuth.getInstance()

        // Initialize Google Sign In first so we can use it for silent sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val isAfterLogout = intent.getBooleanExtra(EXTRA_AFTER_LOGOUT, false)
        
        if (isAfterLogout) {
            auth.signOut()
            // Flag that we can use biometrics for the next login
            LoginPreferences.setBiometricEnabled(this, true)
            googleSignInClient.signOut()
        }

        // Auto-navigate if already logged in and not just logged out
        if (auth.currentUser != null && !isAfterLogout) {
            navigateAfterAuth()
            return
        }

        enableEdgeToEdge()
        setContent {
            HanapAralTheme(dynamicColor = false) {
                SignInScreen(
                    canUseEmailPasswordLogin = LoginPreferences.canUseEmailPasswordLogin(this),
                    onGoogleSignInClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                    onRestrictedEmailLoginClick = {
                        Toast.makeText(this, "Please sign in with Google first.", Toast.LENGTH_LONG).show()
                    },
                    onEmailPasswordSignIn = { email, password ->
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) navigateAfterAuth()
                                else Toast.makeText(this, formatFirebaseAuthError(task.exception), Toast.LENGTH_LONG).show()
                            }
                    }
                )
            }
        }

        // Show Biometric Prompt if the user just logged out or returns
        if (LoginPreferences.isBiometricEnabled(this)) {
            BiometricHelper(this).showBiometricPrompt(
                title = "Welcome Back",
                subtitle = "Authenticate to sign in quickly",
                onSuccess = { 
                    // Perform silent sign in so they don't have to pick account
                    trySilentSignIn()
                },
                onError = { Log.d("Biometric", "Prompt hidden or failed: $it") }
            )
        }
    }

    private fun trySilentSignIn() {
        googleSignInClient.silentSignIn().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val account = task.result
                firebaseAuthWithGoogle(account.idToken!!)
            } else {
                // If silent sign-in fails, they just use the normal buttons
                Log.w("LoginActivity", "Silent sign-in failed", task.exception)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Once logged in, biometrics is "ready" for the next logout
                    LoginPreferences.setBiometricEnabled(this, true)
                    navigateAfterAuth()
                } else {
                    Toast.makeText(this, formatFirebaseAuthError(task.exception), Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateAfterAuth() {
        val uid = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("UserProfile").document(uid).get()
            .addOnSuccessListener { doc ->
                val hasProfile = doc.exists() && doc.getString("fullname")?.isNotBlank() == true
                val destination = if (hasProfile) MainActivity::class.java else ProfileActivity::class.java
                startActivity(Intent(this, destination))
                finish()
            }
    }

    private fun formatFirebaseAuthError(e: Exception?): String {
        return (e as? FirebaseAuthException)?.message ?: e?.message ?: "Authentication failed."
    }
}
