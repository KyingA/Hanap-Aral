package com.example.hanaparal.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveStatus = MutableStateFlow<Boolean?>(null)
    val saveStatus: StateFlow<Boolean?> = _saveStatus.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    fun clearProfileError() {
        _profileError.value = null
    }

    /** Whether this account already has email/password linked (e.g. returning user). */
    fun hasEmailPasswordProvider(): Boolean {
        return auth.currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
    }

    /**
     * Saves profile after Google sign-in. Links email/password when missing so the user can
     * sign in with email next time ([com.example.hanaparal.utils.LoginPreferences] is set by the UI on success).
     */
    fun completeProfileWithPassword(profileData: UserProfile, password: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _saveStatus.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _saveStatus.value = null
            _profileError.value = null
            try {
                val user = auth.currentUser!!
                val email = user.email?.takeIf { it.isNotBlank() } ?: profileData.email
                if (email.isBlank()) {
                    _profileError.value = "Email is required to set a password."
                    _saveStatus.value = false
                    return@launch
                }
                if (!hasEmailPasswordProvider()) {
                    if (password.length < 6) {
                        _profileError.value = "Password must be at least 6 characters."
                        _saveStatus.value = false
                        return@launch
                    }
                    val credential = EmailAuthProvider.getCredential(email, password)
                    user.linkWithCredential(credential).await()
                } else if (password.isNotBlank()) {
                    if (password.length < 6) {
                        _profileError.value = "Password must be at least 6 characters."
                        _saveStatus.value = false
                        return@launch
                    }
                    user.updatePassword(password).await()
                }
                val createdAt = _userProfile.value?.createdAt?.takeIf { it > 0 } ?: System.currentTimeMillis()
                val profileWithUid = profileData.copy(uid = uid, createdAt = createdAt)
                db.collection("UserProfile").document(uid).set(profileWithUid).await()
                _userProfile.value = profileWithUid
                _saveStatus.value = true
            } catch (e: FirebaseAuthWeakPasswordException) {
                Log.e("ProfileViewModel", "Weak password", e)
                _profileError.value = "Password is too weak. Use at least 6 characters."
                _saveStatus.value = false
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error completing profile", e)
                _profileError.value = e.message ?: "Something went wrong. Try again."
                _saveStatus.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val document = db.collection("UserProfile").document(uid).get().await()
                if (document.exists()) {
                    val profile = document.toObject(UserProfile::class.java)
                    _userProfile.value = profile
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveUserProfile(profileData: UserProfile) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _saveStatus.value = false
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _saveStatus.value = null
            _profileError.value = null
            try {
                val createdAt = _userProfile.value?.createdAt?.takeIf { it > 0 } ?: System.currentTimeMillis()
                val profileWithUid = profileData.copy(uid = uid, createdAt = createdAt)
                db.collection("UserProfile").document(uid).set(profileWithUid).await()
                _userProfile.value = profileWithUid
                _saveStatus.value = true
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile", e)
                _saveStatus.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetSaveStatus() {
        _saveStatus.value = null
    }
}
