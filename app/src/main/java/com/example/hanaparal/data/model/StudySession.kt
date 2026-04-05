package com.example.hanaparal.data.model

data class StudySession(
    val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0,
    val endTime: Long = 0,
    val location: String = "", // Can be a link or physical location
    val createdBy: String = ""
)
