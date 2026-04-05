package com.example.hanaparal.data.repository

import android.util.Log
import com.example.hanaparal.data.model.GroupAnnouncement
import com.example.hanaparal.data.model.StudyGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
    private const val TAG = "GroupRepository"

    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    /**
     * Snapshot listeners must not use [kotlinx.coroutines.channels.SendChannel.close] with an error:
     * after sign-out, rules return PERMISSION_DENIED and that would crash collectors (e.g. in
     * [com.example.hanaparal.ui.group.GroupViewModel]). Emit an empty list instead.
     */
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
