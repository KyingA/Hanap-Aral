package com.example.hanaparal.data.model

data class GroupFile(
    val id: String = "",
    val groupId: String = "",
    val fileName: String = "",
    val fileUrl: String = "",
    val fileType: String = "", // e.g., "pdf", "docx", "image/jpeg"
    val fileSize: Long = 0,
    val uploadedBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
