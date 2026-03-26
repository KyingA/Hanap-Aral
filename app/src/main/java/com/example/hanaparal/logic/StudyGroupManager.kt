package com.example.hanaparal.logic

import com.example.hanaparal.data.model.StudyGroup

class StudyGroupManager {

    private val studyGroups = mutableListOf<StudyGroup>()

    // CREATE GROUP
    fun createGroup(
        name: String,
        description: String,
        subject: String,
        adminId: String,
        maxMembers: Int
    ): StudyGroup {

        val group = StudyGroup(
            id = "group_${studyGroups.size + 1}",
            name = name,
            description = description,
            subject = subject,
            adminId = adminId,
            memberIds = listOf(adminId), // auto admin + member
            maxMembers = maxMembers
        )

        studyGroups.add(group)
        return group
    }

    // READ (DISPLAY)
    fun getAllGroups(): List<StudyGroup> {
        return studyGroups
    }

    // JOIN GROUP
    fun joinGroup(groupId: String, userId: String): String {
        val group = studyGroups.find { it.id == groupId }

        return when {
            group == null -> "Group not found"
            group.memberIds.contains(userId) -> "Already a member"
            group.memberIds.size >= group.maxMembers -> "Group is full"
            else -> {
                val updatedGroup = group.copy(memberIds = group.memberIds + userId)
                val index = studyGroups.indexOf(group)
                studyGroups[index] = updatedGroup
                "Joined successfully"
            }
        }
    }

    // CHECK ADMIN
    fun isAdmin(groupId: String, userId: String): Boolean {
        val group = studyGroups.find { it.id == groupId }
        return group?.adminId == userId
    }

    // DELETE GROUP (ADMIN ONLY)
    fun deleteGroup(groupId: String, userId: String): String {
        val group = studyGroups.find { it.id == groupId }

        return if (group != null && group.adminId == userId) {
            studyGroups.remove(group)
            "Group deleted"
        } else {
            "Only admin can delete"
        }
    }

    // EDIT GROUP (ADMIN ONLY)
    fun editGroup(
        groupId: String,
        userId: String,
        newName: String,
        newSubject: String
    ): String {
        val group = studyGroups.find { it.id == groupId }

        return if (group != null && group.adminId == userId) {
            group.name = newName
            group.subject = newSubject
            "Group updated"
        } else {
            "Only admin can edit"
        }
    }

    // SEARCH (OPTIONAL)
    fun searchGroups(keyword: String): List<StudyGroup> {
        return studyGroups.filter {
            it.name.contains(keyword, true) ||
            it.subject.contains(keyword, true)
        }
    }
}
