package com.example.hanaparal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.hanaparal.data.config.RemoteConfigManager
import com.google.firebase.auth.FirebaseAuth

class MainViewModel : ViewModel() {

    private val rcManager = RemoteConfigManager()

    private val _isGroupCreationEnabled = mutableStateOf(true)
    val isGroupCreationEnabled: State<Boolean> = _isGroupCreationEnabled

    private val _announcementHeader = mutableStateOf("")
    val announcementHeader: State<String> = _announcementHeader

    private val _maxMembersPerGroup = mutableStateOf(10L)
    val maxMembersPerGroup: State<Long> = _maxMembersPerGroup

    private val _isAdminMode = mutableStateOf(false)
    val isAdminMode: State<Boolean> = _isAdminMode

    private val _isSuperAdmin = mutableStateOf(false)
    val isSuperAdmin: State<Boolean> = _isSuperAdmin

    init {
        refreshFromRemoteConfig()
        rcManager.fetchAndActivate { refreshFromRemoteConfig() }
    }

    fun refreshFromRemoteConfig() {
        _isGroupCreationEnabled.value = rcManager.isGroupCreationEnabled()
        _announcementHeader.value = rcManager.getAnnouncementHeader()
        _maxMembersPerGroup.value = rcManager.getMaxGroupMembers()
        _isSuperAdmin.value = rcManager.isSuperAdmin(FirebaseAuth.getInstance().currentUser?.uid)
    }

    fun fetchRemoteConfig() {
        rcManager.fetchAndActivate { refreshFromRemoteConfig() }
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun updateGroupCreation(enabled: Boolean) {
        _isGroupCreationEnabled.value = enabled
    }
}
