package com.example.hanaparal.model

data class UserProfile(
    val uid: String = "",
    val fullname: String = "",
    val program: String = "",
    val email: String = "",
    val quietStudy: Boolean = false,
    val peerTeaching: Boolean = false,
    val projectBased: Boolean = false,
    val profileImage: String = "doggy",
    val createdAt: Long = System.currentTimeMillis()
)
