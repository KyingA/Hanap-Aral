package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.StudyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton repository to maintain state across different Activities.
 */
object GroupRepository {
    private val _mockGroups = MutableStateFlow(listOf(
        StudyGroup(
            id = "1",
            name = "Data Structures 101",
            description = "Mastering trees, graphs, and algorithms.",
            subject = "CS Core",
            adminId = "user1",
            memberIds = listOf("user1", "user2", "user3"),
            maxMembers = 5,
            schedule = "Mon, Wed 4:00 PM",
            status = "ACTIVE"
        ),
        StudyGroup(
            id = "2",
            name = "C++ Study Circle",
            description = "Learning advanced C++ features and STL.",
            subject = "Programming",
            adminId = "user2",
            memberIds = listOf("user2", "user4", "user5"),
            maxMembers = 15,
            schedule = "Fri 2:00 PM",
            status = "ACTIVE"
        )
    ))

    fun getGroups(): Flow<List<StudyGroup>> = _mockGroups.asStateFlow()

    suspend fun createGroup(group: StudyGroup): Result<String> {
        val currentList = _mockGroups.value.toMutableList()
        val id = (currentList.size + 1).toString()
        val newGroup = group.copy(id = id)
        currentList.add(newGroup)
        _mockGroups.value = currentList
        return Result.success(id)
    }

    suspend fun joinGroup(groupId: String, userId: String): Result<Unit> {
        val currentList = _mockGroups.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == groupId }
        if (index == -1) return Result.failure(Exception("Group not found"))
        
        val group = currentList[index]
        
        // Member Limit Enforcement
        if (group.memberIds.size >= group.maxMembers) {
            return Result.failure(Exception("This group is already full (${group.maxMembers}/${group.maxMembers})"))
        }
        
        if (group.memberIds.contains(userId)) {
            return Result.failure(Exception("You are already a member"))
        }

        currentList[index] = group.copy(memberIds = group.memberIds + userId)
        _mockGroups.value = currentList
        return Result.success(Unit)
    }

    suspend fun leaveGroup(groupId: String, userId: String): Result<Unit> {
        val currentList = _mockGroups.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == groupId }
        if (index == -1) return Result.failure(Exception("Group not found"))

        val group = currentList[index]
        val updatedMembers = group.memberIds.filter { it != userId }
        
        if (updatedMembers.isEmpty()) {
            currentList.removeAt(index)
        } else {
            var newAdminId = group.adminId
            if (group.adminId == userId) newAdminId = updatedMembers.first()
            currentList[index] = group.copy(memberIds = updatedMembers, adminId = newAdminId)
        }

        _mockGroups.value = currentList
        return Result.success(Unit)
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        val currentList = _mockGroups.value.toMutableList()
        val removed = currentList.removeIf { it.id == groupId }
        if (!removed) return Result.failure(Exception("Group not found"))
        _mockGroups.value = currentList
        return Result.success(Unit)
    }

    suspend fun updateGroup(updatedGroup: StudyGroup): Result<Unit> {
        val currentList = _mockGroups.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedGroup.id }
        if (index == -1) return Result.failure(Exception("Group not found"))
        currentList[index] = updatedGroup
        _mockGroups.value = currentList
        return Result.success(Unit)
    }
}
