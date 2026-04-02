package com.example.hanaparal.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel(private val repository: GroupRepository = GroupRepository()) : ViewModel() {
    private val _groups = MutableStateFlow<List<StudyGroup>>(emptyList())
    val groups: StateFlow<List<StudyGroup>> = _groups.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Mock Current User ID
    val currentUserId = "user1" 

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            repository.getGroups().collect {
                _groups.value = it
            }
        }
    }

    fun createGroup(name: String, subject: String, description: String, maxMembers: Int = 20) {
        val newGroup = StudyGroup(
            id = "", 
            name = name,
            subject = subject,
            description = description,
            adminId = currentUserId, // Automatic admin assignment (creator)
            memberIds = listOf(currentUserId), // Creator is the first member
            maxMembers = maxMembers,
            schedule = "TBD",
            status = "ACTIVE"
        )
        viewModelScope.launch {
            repository.createGroup(newGroup)
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            val result = repository.joinGroup(groupId, currentUserId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
