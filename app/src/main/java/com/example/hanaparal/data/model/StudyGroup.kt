package com.example.hanaparal.data.model

data class StudyGroup(
    val id: String,
    var name: String,
    var description: String,
    var subject: String,
    var adminId: String,
    var memberIds: List<String>,
    var maxMembers: Int
)
