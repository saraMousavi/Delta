// ChatModels.kt
package com.example.delta.data.entity


data class ChatManagerDto(
    val userId: Long,
    val firstName: String?,
    val lastName: String?,
    val mobileNumber: String?,
    val buildingId: Long?,
    val buildingName: String?
) {
    val fullName: String
        get() {
            val name = listOfNotNull(firstName, lastName).joinToString(" ").trim()
            return if (name.isNotEmpty()) name else (mobileNumber ?: "")
        }

    val displaySubtitle: String
        get() {
            val phone = mobileNumber ?: ""
            val b = buildingName ?: ""
            return listOf(phone, b).filter { it.isNotBlank() }.joinToString(" - ")
        }
}

data class ChatThreadDto(
    val threadId: Long,
    val buildingId: Long?,
    val buildingName: String?,
    val participants: List<Long>,
    val lastMessageAt: Long?,
    val lastMessageText: String?,
    val partnerId: Long?,
    val partnerFirstName: String?,
    val partnerLastName: String?,
    val partnerMobileNumber: String?,
    val unreadCount: Int
) {
    val partnerFullName: String
        get() {
            val name = listOfNotNull(partnerFirstName, partnerLastName).joinToString(" ").trim()
            return if (name.isNotEmpty()) name else (partnerMobileNumber ?: "")
        }

    val partnerSubtitle: String
        get() {
            val phone = partnerMobileNumber ?: ""
            val b = buildingName ?: ""
            return listOf(phone, b).filter { it.isNotBlank() }.joinToString(" - ")
        }
}

data class ChatMessageDto(
    val messageId: Long,
    val threadId: Long,
    val senderId: Long,
    val text: String,
    val createdAt: Long
)

data class ChatUiState(
    val thread: ChatThreadDto?,
    val peerId: Long?,
    val peerName: String?,
    val messages: List<ChatMessageDto>,
    val isLoading: Boolean,
    val error: String?
)

