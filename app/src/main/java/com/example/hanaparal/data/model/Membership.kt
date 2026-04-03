package com.example.hanaparal.data.model

data class Membership(
    val id: String = "",
    val userId: String = "",
    val groupId: String = "",
    val role: String = "MEMBER", // "ADMIN" or "MEMBER"
    val joinedAt: Long = System.currentTimeMillis()
)
