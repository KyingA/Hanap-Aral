package com.example.hanaparal.data.repository

import android.util.Log
import com.example.hanaparal.data.model.GroupAnnouncement
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class ChatMessage(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object GroupRepository {
    private const val TAG = "GroupRepository"

    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    private fun logSnapshotError(where: String, error: Throwable) {
        val code = (error as? FirebaseFirestoreException)?.code?.name
        Log.w(TAG, "Firestore listener $where failed${if (code != null) " ($code)" else ""}", error)
    }

    fun getGroups(): Flow<List<StudyGroup>> = callbackFlow {
        val subscription = groupsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                logSnapshotError("groups", error)
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val groups = snapshot.toObjects(StudyGroup::class.java)
                trySend(groups)
            }
        }
        awaitClose { subscription.remove() }
    }

    fun getAnnouncements(groupId: String): Flow<List<GroupAnnouncement>> = callbackFlow {
        val subscription = groupsCollection.document(groupId).collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logSnapshotError("announcements/$groupId", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(GroupAnnouncement::class.java)?.copy(
                            id = doc.id,
                            groupId = groupId
                        )
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun postAnnouncement(
        groupId: String,
        title: String,
        body: String,
        createdBy: String
    ): Result<Unit> {
        return try {
            val ref = groupsCollection.document(groupId).collection("announcements").document()
            val item = GroupAnnouncement(
                id = ref.id,
                groupId = groupId,
                title = title.trim(),
                body = body.trim(),
                createdAt = System.currentTimeMillis(),
                createdBy = createdBy
            )
            ref.set(item).await()

            // NOTIFY ALL MEMBERS
            val groupDoc = groupsCollection.document(groupId).get().await()
            val group = groupDoc.toObject(StudyGroup::class.java)
            group?.memberIds?.forEach { memberId ->
                if (memberId != createdBy) {
                    NotificationRepository.addNotificationForUser(
                        memberId,
                        "Announcement in ${group.name}",
                        title.trim()
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessages(groupId: String): Flow<List<ChatMessage>> = callbackFlow {
        val subscription = groupsCollection.document(groupId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    logSnapshotError("messages/$groupId", error)
                    trySend(emptyList())
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

        // NOTIFY ALL MEMBERS (except sender)
        val groupDoc = groupsCollection.document(groupId).get().await()
        val group = groupDoc.toObject(StudyGroup::class.java)
        
        // Use "users" collection (standardized)
        val senderDoc = firestore.collection("users").document(senderId).get().await()
        val senderName = senderDoc.getString("fullname") ?: "A member"

        group?.memberIds?.forEach { memberId ->
            if (memberId != senderId) {
                NotificationRepository.addNotificationForUser(
                    memberId,
                    "New message in ${group.name}",
                    "$senderName: $text"
                )
            }
        }
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
                // Use "users" collection (standardized)
                val userDoc = firestore.collection("users").document(userId).get().await()
                val userName = userDoc.getString("fullname") ?: "A new member"

                // Update members
                docRef.update("memberIds", group.memberIds + userId).await()

                // NOTIFY ALL EXISTING MEMBERS (including admin)
                group.memberIds.forEach { memberId ->
                    NotificationRepository.addNotificationForUser(
                        memberId,
                        "New Member Joined!",
                        "$userName has joined ${group.name}"
                    )
                }
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
