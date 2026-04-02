package com.example.hanaparal.data.repository

import com.example.hanaparal.data.model.StudyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupRepository {
    companion object {
        private val _mockGroups = MutableStateFlow(listOf(
            StudyGroup(
                id = "1",
                name = "Data Structures 101",
                description = "Mastering trees, graphs, and algorithms.",
                subject = "CS Core",
                adminId = "user1",
                memberIds = listOf("user1", "user2", "user3"),
                maxMembers = 20,
                schedule = "Wed, 4PM",
                status = "ACTIVE"
            ),
            StudyGroup(
                id = "2",
                name = "C++ Study Circle",
                description = "Learning advanced C++ features and STL.",
                subject = "Programming",
                adminId = "user2",
                memberIds = listOf("user2", "user4", "user5", "user6"),
                maxMembers = 15,
                schedule = "Fri, 2PM",
                status = "ACTIVE"
            ),
            StudyGroup(
                id = "3",
                name = "Algorithms Mastery",
                description = "Weekly deep dives into Big O notation and sorting optimizations.",
                subject = "CS CORE",
                adminId = "user3",
                memberIds = listOf("user3", "user7"),
                maxMembers = 30,
                schedule = "Mon, 5PM",
                status = "ACTIVE"
            ),
            StudyGroup(
                id = "4",
                name = "Mobile App Dev",
                description = "Kotlin and Compose focus for building modern Android apps.",
                subject = "CS Elective",
                adminId = "user4",
                memberIds = listOf("user4", "user8"),
                maxMembers = 25,
                schedule = "Tue, 1PM",
                status = "ACTIVE"
            ),
            StudyGroup(
                id = "5",
                name = "Discrete Math Hub",
                description = "Solving logic puzzles and set theory problems together.",
                subject = "Math",
                adminId = "user5",
                memberIds = listOf("user5", "user9", "user10"),
                maxMembers = 10,
                schedule = "Thu, 10AM",
                status = "ACTIVE"
            ),
            StudyGroup(
                id = "6",
                name = "UI/UX Design Lab",
                description = "Prototyping and user testing for student projects.",
                subject = "Design",
                adminId = "user6",
                memberIds = listOf("user6"),
                maxMembers = 12,
                schedule = "Sat, 2PM",
                status = "ACTIVE"
            )
        ))
    }

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
        
        if (group.memberIds.size >= group.maxMembers) {
            return Result.failure(Exception("Group is full (Limit: ${group.maxMembers})"))
        }
        
        if (group.memberIds.contains(userId)) {
            return Result.failure(Exception("You are already a member"))
        }

        currentList[index] = group.copy(memberIds = group.memberIds + userId)
        _mockGroups.value = currentList
        return Result.success(Unit)
    }
}
