package com.example.hanaparal.data.model

data class UserProfile(
    val uid: String = "",
    val fullname: String = "",
    val program: String = "",
    val email: String = "",
    val profileImage: String = "",
    val quietStudy: Boolean = false,
    val peerTeaching: Boolean = false,
    val projectBased: Boolean = false,
    val createdAt: Long = 0L
)
