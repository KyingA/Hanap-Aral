package com.example.hanaparal.data.config

import android.content.Context
import com.example.hanaparal.HanapAralApplication
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

/**
 * Firebase Remote Config with optional **local overrides** (applied from Super Admin screen).
 * Remote keys must match the Firebase console / defaults below.
 */
class RemoteConfigManager(
    private val appContext: Context = HanapAralApplication.appContext
) {

    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_GROUP_CREATION_ENABLED to true,
                KEY_ANNOUNCEMENT_HEADER to "",
                KEY_MAX_MEMBERS_PER_GROUP to 10L,
                KEY_SUPER_ADMIN_ID to "3JZKIY3OYIVHOBdUh1GiwyDn8NB2"
            )
        )
    }

    fun fetchAndActivate(onComplete: (Boolean) -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
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
        return remoteConfig.getString(KEY_ANNOUNCEMENT_HEADER)
    }

    fun getMaxGroupMembers(): Long {
        if (prefs.getBoolean(KEY_OV_MAX_MEMBERS_SET, false)) {
            return prefs.getLong(KEY_OV_MAX_MEMBERS, 10L)
        }
        return remoteConfig.getLong(KEY_MAX_MEMBERS_PER_GROUP)
    }

    fun isSuperAdmin(userId: String?): Boolean {
        val adminId = remoteConfig.getString(KEY_SUPER_ADMIN_ID)
        return userId != null && userId == adminId
    }

    /**
     * Persists admin UI values for this install (client cannot push to Firebase RC server).
     */
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
    }

    companion object {
        private const val PREFS_NAME = "hanap_aral_rc_overrides"

        const val KEY_GROUP_CREATION_ENABLED = "is_group_creation_enabled"
        const val KEY_ANNOUNCEMENT_HEADER = "announcement_header"
        const val KEY_MAX_MEMBERS_PER_GROUP = "max_members_per_group"
        private const val KEY_SUPER_ADMIN_ID = "super_admin_id"

        private const val KEY_OV_GROUP_CREATE_SET = "ov_group_create_set"
        private const val KEY_OV_GROUP_CREATE = "ov_group_create"
        private const val KEY_OV_ANNOUNCE_SET = "ov_announce_set"
        private const val KEY_OV_ANNOUNCE = "ov_announce"
        private const val KEY_OV_MAX_MEMBERS_SET = "ov_max_members_set"
        private const val KEY_OV_MAX_MEMBERS = "ov_max_members"
    }
}
