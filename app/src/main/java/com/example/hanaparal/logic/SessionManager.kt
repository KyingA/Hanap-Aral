package com.example.hanaparal.logic

class SessionManager {

    private var currentUserId: String = "user1"

    fun login(userId: String) {
        currentUserId = userId
    }

    fun getCurrentUser(): String {
        return currentUserId
    }
}
