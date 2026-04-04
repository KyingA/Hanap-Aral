package com.example.hanaparal.data.model

data class Announcement(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val content: String = "",
    val authorId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
