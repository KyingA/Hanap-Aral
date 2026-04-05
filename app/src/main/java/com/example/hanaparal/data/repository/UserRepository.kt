package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: "user1"

    suspend fun saveProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.userId).set(user).await()
            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadProfile(userId: String): User? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            _currentUser.value = user
            user
        } catch (e: Exception) {
            null
        }
    }
}
