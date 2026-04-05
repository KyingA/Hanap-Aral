package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object GroupRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    fun getGroups(): Flow<List<StudyGroup>> = callbackFlow {
        val subscription = groupsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val groups = snapshot.toObjects(StudyGroup::class.java)
                trySend(groups)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getMessages(groupId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = groupsCollection.document(groupId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(ChatMessage::class.java)
                    trySend(messages)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(groupId: String, senderId: String, text: String) {
        val docRef = groupsCollection.document(groupId).collection("messages").document()
        val newMessage = ChatMessage(
            id = docRef.id,
            groupId = groupId,
            senderId = senderId,
            text = text
        )
        docRef.set(newMessage).await()
    }

    suspend fun createGroup(group: StudyGroup): Result<StudyGroup> {
        return try {
            val docRef = groupsCollection.document()
            val newGroup = group.copy(id = docRef.id)
            docRef.set(newGroup).await()
            Result.success(newGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val docRef = groupsCollection.document(groupId)
            val snapshot = docRef.get().await()
            val group = snapshot.toObject(StudyGroup::class.java) ?: throw Exception("Group not found")
            
            if (!group.memberIds.contains(userId)) {
                docRef.update("memberIds", group.memberIds + userId).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            val docRef = groupsCollection.document(groupId)
            val snapshot = docRef.get().await()
            val group = snapshot.toObject(StudyGroup::class.java) ?: throw Exception("Group not found")
            
            val updatedMembers = group.memberIds.filter { it != userId }
            if (updatedMembers.isEmpty()) {
                docRef.delete().await()
            } else {
                var newAdminId = group.adminId
                if (group.adminId == userId) newAdminId = updatedMembers.first()
                docRef.update("memberIds", updatedMembers, "adminId", newAdminId).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGroup(updatedGroup: StudyGroup): Result<Unit> {
        return try {
            groupsCollection.document(updatedGroup.id).set(updatedGroup).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
