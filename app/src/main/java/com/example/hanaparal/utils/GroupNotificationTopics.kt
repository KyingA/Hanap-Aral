package com.example.hanaparal.utils

/**
 * FCM topic per study group. Must stay in sync with [functions] Cloud Messaging topic logic.
 * Allowed chars: [a-zA-Z0-9-_.~%]
 */
object GroupNotificationTopics {
    fun topicForGroupId(groupId: String): String {
        val safe = groupId.replace(Regex("[^a-zA-Z0-9\\-_.~%]"), "_").take(180)
        return "hg_$safe"
    }
}
