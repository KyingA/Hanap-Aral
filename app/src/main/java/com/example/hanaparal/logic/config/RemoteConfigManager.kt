package com.example.hanaparal.logic.config

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class RemoteConfigManager {

    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf(
            KEY_GROUP_CREATION_ENABLED to true,
            KEY_ANNOUNCEMENT_HEADER to "Welcome to Hanap-Aral!",
            KEY_MAX_GROUP_MEMBERS to 10L,
            KEY_SUPER_ADMIN_ID to "3JZKIY3OYIVHOBdUh1GiwyDn8NB2"
        ))
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun isGroupCreationEnabled(): Boolean = remoteConfig.getBoolean(KEY_GROUP_CREATION_ENABLED)
    
    fun getAnnouncementHeader(): String = remoteConfig.getString(KEY_ANNOUNCEMENT_HEADER)
    
    fun getMaxGroupMembers(): Long = remoteConfig.getLong(KEY_MAX_GROUP_MEMBERS)

    fun isSuperAdmin(userId: String?): Boolean {
        val adminId = remoteConfig.getString(KEY_SUPER_ADMIN_ID)
        return userId != null && userId == adminId
    }

    companion object {
        private const val KEY_GROUP_CREATION_ENABLED = "is_group_creation_enabled"
        private const val KEY_ANNOUNCEMENT_HEADER = "announcement_header"
        private const val KEY_MAX_GROUP_MEMBERS = "max_group_members"
        private const val KEY_SUPER_ADMIN_ID = "super_admin_id"
    }
}
