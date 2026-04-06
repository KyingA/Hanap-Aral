package com.example.hanaparal.data.model

/** Stored under `groups/{groupId}/announcements/{id}` in Firestore. */
data class GroupAnnouncement(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
