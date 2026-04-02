package com.example.hanaparal.data.model

data class StudyGroup(
    val id: String,
    val name: String,
    val description: String,
    val subject: String,
    val adminId: String,
    val memberIds: List<String>,
    val maxMembers: Int,
    val schedule: String = "TBD",
    val status: String = "ACTIVE"
)
