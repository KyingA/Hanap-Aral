package com.example.hanaparal.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

object UserProfileRepository {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Loads [com.example.hanaparal.data.model.UserProfile.profileImage] drawable names for each user id
     * (documents in `UserProfile/{uid}`).
     */
    suspend fun getProfileImageKeysByUserIds(userIds: List<String>): Map<String, String> {
        val distinct = userIds.distinct()
        if (distinct.isEmpty()) return emptyMap()
        return coroutineScope {
            distinct.map { uid ->
                async {
                    uid to runCatching {
                        val doc = db.collection("UserProfile").document(uid).get().await()
                        doc.getString("profileImage").orEmpty()
                    }.getOrDefault("")
                }
            }.awaitAll().toMap()
        }
    }
}
