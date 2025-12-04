// ChatModels.kt
package com.example.delta.data.entity


data class ChatManagerDto(
    val userId: Long,
    val firstName: String?,
    val lastName: String?,
    val mobileNumber: String?
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName)
            .joinToString(" ")
            .ifBlank { mobileNumber ?: "Manager $userId" }
}

data class ChatThreadDto(
    val threadId: Long,
    val buildingId: Long?,
    val participants: List<Long> = emptyList(),
    val userId: Long? = null,
    val managerId: Long? = null,
    val lastMessageAt: Long? = null,
    val lastMessageText: String? = null,
    val unreadCount: Int = 0,
    val partner: ChatManagerDto? = null
)

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

