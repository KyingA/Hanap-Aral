package com.example.hanaparal.data.remote

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.example.hanaparal.R

class RemoteConfigSource {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Setting defaults from code. Alternatively, use remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        val defaults: Map<String, Any> = mapOf(
            "is_group_creation_enabled" to true,
            "announcement_header" to "Welcome back! 👋",
            "max_members_per_group" to 10L
        )
        remoteConfig.setDefaultsAsync(defaults)
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun isGroupCreationEnabled(): Boolean = remoteConfig.getBoolean("is_group_creation_enabled")
    fun getAnnouncementHeader(): String = remoteConfig.getString("announcement_header")
    fun getMaxMembersPerGroup(): Long = remoteConfig.getLong("max_members_per_group")
}
