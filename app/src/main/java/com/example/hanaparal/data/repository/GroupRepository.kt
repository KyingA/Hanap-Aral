package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.StudyGroup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GroupRepository {
    // Simulated database for now. In a real app, this would use FirebaseFirestore.
    private val mockGroups = mutableListOf(
        StudyGroup(
            id = "1",
            name = "Mobile App Development",
            description = "Learning Kotlin and Compose",
            subject = "Computer Science",
            adminId = "admin1",
            memberIds = listOf("admin1", "user2"),
            maxMembers = 5
        ),
        StudyGroup(
            id = "2",
            name = "Data Science 101",
            description = "Python and Machine Learning basics",
            subject = "Mathematics",
            adminId = "admin2",
            memberIds = listOf("admin2"),
            maxMembers = 10
        )
    )

    fun getGroups(): Flow<List<StudyGroup>> = flow {
        while(true) {
            emit(mockGroups.toList())
            delay(2000) // Poll for changes
        }
    }

    suspend fun createGroup(group: StudyGroup): Result<String> {
        val id = (mockGroups.size + 1).toString()
        val newGroup = group.copy(id = id)
        mockGroups.add(newGroup)
        return Result.success(id)
    }

    suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        val index = mockGroups.indexOfFirst { it.id == groupId }
        if (index == -1) return Result.failure(Exception("Group not found"))
        
        val group = mockGroups[index]
        
        // Member limit enforcement
        if (group.memberIds.size >= group.maxMembers) {
            return Result.failure(Exception("Group is full (Limit: ${group.maxMembers})"))
        }
        
        if (group.memberIds.contains(userId)) {
            return Result.failure(Exception("You are already a member"))
        }

        mockGroups[index] = group.copy(memberIds = group.memberIds + userId)
        return Result.success(Unit)
    }
}
