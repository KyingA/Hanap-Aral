package com.example.hanaparal.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
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
            try {
                val profileWithUid = profileData.copy(uid = uid)
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
