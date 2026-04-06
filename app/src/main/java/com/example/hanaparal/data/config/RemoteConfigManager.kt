package com.example.hanaparal.data.config

import android.content.Context
import android.util.Log
import com.example.hanaparal.HanapAralApplication
import com.example.hanaparal.R
import com.example.hanaparal.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

/**
 * Modern Firebase Remote Config Manager.
 */
class RemoteConfigManager(
    private val appContext: Context = HanapAralApplication.appContext
) {

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun isGroupCreationEnabled(): Boolean {
        if (prefs.getBoolean(KEY_OV_GROUP_CREATE_SET, false)) {
            return prefs.getBoolean(KEY_OV_GROUP_CREATE, true)
        }
        return remoteConfig.getBoolean(KEY_GROUP_CREATION_ENABLED)
    }

    fun getAnnouncementHeader(): String {
        if (prefs.getBoolean(KEY_OV_ANNOUNCE_SET, false)) {
            return prefs.getString(KEY_OV_ANNOUNCE, "") ?: ""
        }
        return remoteConfig.getString(KEY_ANNOUNCEMENT)
    }

    fun getMaxGroupMembers(): Long {
        if (prefs.getBoolean(KEY_OV_MAX_MEMBERS_SET, false)) {
            return prefs.getLong(KEY_OV_MAX_MEMBERS, 10L)
        }
        return remoteConfig.getLong(KEY_MAX_MEMBERS_PER_GROUP)
    }

    /**
     * Checks if user is super admin.
     */
    fun isSuperAdmin(userId: String?): Boolean {
        if (userId == null) return false
        
        // 1. Check current Remote Config value (loaded from server or defaults)
        val adminId = remoteConfig.getString(KEY_SUPER_ADMIN_ID)
        
        // 2. Hardcoded fallback for your specific testing UID to guarantee it works for you
        val hardcodedAdminId = "MMpHLUv7hedOpGTJu0STzj2Lavx2"
        
        val isMatch = (userId == adminId) || (userId == hardcodedAdminId)
        
        Log.d("RemoteConfig", "SuperAdmin Check -> Current: $userId, Target: $adminId, Match: $isMatch")
        
        return isMatch
    }

    fun applyLocalOverrides(
        groupCreationEnabled: Boolean,
        announcementHeader: String,
        maxGroupMembers: Long
    ) {
        prefs.edit()
            .putBoolean(KEY_OV_GROUP_CREATE_SET, true)
            .putBoolean(KEY_OV_GROUP_CREATE, groupCreationEnabled)
            .putBoolean(KEY_OV_ANNOUNCE_SET, true)
            .putString(KEY_OV_ANNOUNCE, announcementHeader)
            .putBoolean(KEY_OV_MAX_MEMBERS_SET, true)
            .putLong(KEY_OV_MAX_MEMBERS, maxGroupMembers)
            .apply()
        
        Log.d("RemoteConfig", "Local Overrides Applied: Enabled=$groupCreationEnabled, Max=$maxGroupMembers")
    }

    companion object {
        private const val PREFS_NAME = "hanap_aral_rc_overrides"

        const val KEY_GROUP_CREATION_ENABLED = "is_group_creation_enabled"
        const val KEY_ANNOUNCEMENT = "announcement"
        const val KEY_MAX_MEMBERS_PER_GROUP = "max_members_per_group"
        const val KEY_SUPER_ADMIN_ID = "super_admin_id"

        private const val KEY_OV_GROUP_CREATE_SET = "ov_group_create_set"
        private const val KEY_OV_GROUP_CREATE = "ov_group_create"
        private const val KEY_OV_ANNOUNCE_SET = "ov_announce_set"
        private const val KEY_OV_ANNOUNCE = "ov_announce"
        private const val KEY_OV_MAX_MEMBERS_SET = "ov_max_members_set"
        private const val KEY_OV_MAX_MEMBERS = "ov_max_members"
    }
}
