package com.example.hanaparal.ui.group

import com.example.hanaparal.data.model.StudyGroup

class GroupAdapter(private var groups: List<StudyGroup>) {

    fun updateData(newGroups: List<StudyGroup>) {
        groups = newGroups
    }

    fun display() {
        groups.forEach {
            println("${it.name} - ${it.memberIds.size}/${it.maxMembers}")
        }
    }
}
