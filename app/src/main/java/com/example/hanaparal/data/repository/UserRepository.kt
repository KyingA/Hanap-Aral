package com.example.hanaparal.data.repository

import com.google.firebase.auth.FirebaseAuth

object UserRepository {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: "user1"
}
