package com.example.hanaparal.ui.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hanaparal.data.model.GroupAnnouncement
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.ChatMessage
import com.example.hanaparal.data.repository.GroupRepository
import com.example.hanaparal.data.repository.UserProfileRepository
import com.example.hanaparal.data.repository.UserRepository
import com.example.hanaparal.utils.GroupNotificationTopics
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
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

    private val _announcements = MutableStateFlow<List<GroupAnnouncement>>(emptyList())
    val announcements: StateFlow<List<GroupAnnouncement>> = _announcements.asStateFlow()

    /** User id → `UserProfile.profileImage` drawable name (e.g. `doggy`). */
    private val _memberProfileImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val memberProfileImages: StateFlow<Map<String, String>> = _memberProfileImages.asStateFlow()

    private var announcementsJob: Job? = null

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

    fun loadMemberProfileImages(memberIds: List<String>) {
        viewModelScope.launch {
            _memberProfileImages.value = UserProfileRepository.getProfileImageKeysByUserIds(memberIds)
        }
    }

    fun loadAnnouncements(groupId: String) {
        announcementsJob?.cancel()
        _announcements.value = emptyList()
        announcementsJob = viewModelScope.launch {
            GroupRepository.getAnnouncements(groupId).collect { list ->
                _announcements.value = list
            }
        }
    }

    fun postAnnouncement(groupId: String, title: String, body: String, onDone: (Boolean) -> Unit) {
        if (title.isBlank()) {
            _errorMessage.value = "Announcement title is required"
            onDone(false)
            return
        }
        viewModelScope.launch {
            val result = GroupRepository.postAnnouncement(groupId, title, body, currentUserId)
            if (result.isFailure) {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to publish"
            }
            onDone(result.isSuccess)
        }
    }

    private fun subscribeGroupTopic(groupId: String) {
        val topic = GroupNotificationTopics.topicForGroupId(groupId)
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("GroupFCM", "subscribe failed: $topic", task.exception)
                }
            }
    }

    private fun unsubscribeGroupTopic(groupId: String) {
        val topic = GroupNotificationTopics.topicForGroupId(groupId)
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("GroupFCM", "unsubscribe failed: $topic", task.exception)
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
            } else {
                subscribeGroupTopic(groupId)
            }
        }
    }

    fun leaveGroup(groupId: String, onLeft: () -> Unit) {
        viewModelScope.launch {
            val result = GroupRepository.leaveGroup(groupId, currentUserId)
            if (result.isSuccess) {
                unsubscribeGroupTopic(groupId)
                onLeft()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            unsubscribeGroupTopic(groupId)
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
                val id = result.getOrNull()?.id ?: ""
                if (id.isNotEmpty()) subscribeGroupTopic(id)
                onCreated(id)
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
