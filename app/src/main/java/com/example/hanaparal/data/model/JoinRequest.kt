package com.example.hanaparal.data.model

data class JoinRequest(
    val id: String = "",
    val userId: String = "",
    val groupId: String = "",
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)
