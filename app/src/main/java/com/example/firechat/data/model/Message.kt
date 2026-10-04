package com.example.firechat.data.model

import java.util.Date

/** Documento `chats/{chatId}/messages/{messageId}` en Firestore. */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Date? = null
)
