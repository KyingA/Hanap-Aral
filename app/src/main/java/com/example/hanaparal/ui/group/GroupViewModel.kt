package com.example.hanaparal.ui.group

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.ChatMessage
import com.example.hanaparal.data.repository.GroupRepository
import com.example.hanaparal.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroupViewModel : ViewModel() {
    private val _studyGroups = MutableStateFlow<List<StudyGroup>>(emptyList())
    val studyGroups: StateFlow<List<StudyGroup>> = _studyGroups.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val currentUserId: String
        get() = UserRepository.getCurrentUserId()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            GroupRepository.getGroups().collect { groups ->
                _studyGroups.value = groups
            }
        }
    }

    fun loadMessages(groupId: String) {
        viewModelScope.launch {
            GroupRepository.getMessages(groupId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(groupId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            GroupRepository.sendMessage(groupId, currentUserId, text)
        }
    }

    fun joinGroup(groupId: String) {
        viewModelScope.launch {
            val result = GroupRepository.joinGroup(groupId, currentUserId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun leaveGroup(groupId: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            val result = GroupRepository.leaveGroup(groupId, currentUserId)
            if (result.isSuccess) {
                onLeft()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val result = GroupRepository.deleteGroup(groupId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getGroupById(groupId: String): StudyGroup? {
        return _studyGroups.value.find { it.id == groupId }
    }

    fun createGroup(group: StudyGroup, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val result = GroupRepository.createGroup(group)
            if (result.isSuccess) {
                onCreated(result.getOrNull()?.id ?: "")
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun updateGroup(group: StudyGroup, onUpdated: () -> Unit) {
        viewModelScope.launch {
            val result = GroupRepository.updateGroup(group)
            if (result.isSuccess) {
                onUpdated()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }
}
