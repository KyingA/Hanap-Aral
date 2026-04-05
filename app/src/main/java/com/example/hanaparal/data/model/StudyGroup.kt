package com.example.hanaparal.data.model

data class StudyGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val subject: String = "",
    val scheduleDays: List<String> = emptyList(),
    val timeStart: String = "",
    val timeEnd: String = "",
    val adminId: String = "",
    val memberIds: List<String> = emptyList(),
    val maxMembers: Int = 20,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to get formatted schedule string
    val formattedSchedule: String
        get() = if (scheduleDays.isNotEmpty() && timeStart.isNotEmpty()) {
            "${scheduleDays.joinToString(", ")} • $timeStart - $timeEnd"
        } else {
            "TBD"
        }
}
