package com.example.hanaparal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class MainViewModel : ViewModel() {
    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    private val _isGroupCreationEnabled = mutableStateOf(true)
    val isGroupCreationEnabled: State<Boolean> = _isGroupCreationEnabled

    private val _announcementHeader = mutableStateOf("Welcome back! 👋")
    val announcementHeader: State<String> = _announcementHeader

    private val _maxMembersPerGroup = mutableStateOf(10L)
    val maxMembersPerGroup: State<Long> = _maxMembersPerGroup

    // Admin Mode state for Superuser controls
    private val _isAdminMode = mutableStateOf(false)
    val isAdminMode: State<Boolean> = _isAdminMode

    init {
        setupRemoteConfig()
    }

    private fun setupRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        val defaults: Map<String, Any> = mapOf(
            "is_group_creation_enabled" to true,
            "announcement_header" to "Welcome back! 👋",
            "max_members_per_group" to 10L
        )
        remoteConfig.setDefaultsAsync(defaults)
        
        fetchRemoteConfig()
    }

    fun fetchRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateStates()
                }
            }
    }

    private fun updateStates() {
        _isGroupCreationEnabled.value = remoteConfig.getBoolean("is_group_creation_enabled")
        _announcementHeader.value = remoteConfig.getString("announcement_header")
        _maxMembersPerGroup.value = remoteConfig.getLong("max_members_per_group")
    }

    // Superuser methods
    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun updateGroupCreation(enabled: Boolean) {
        // In a real app, you might push this to a backend or just override locally for this session
        _isGroupCreationEnabled.value = enabled
    }
}
