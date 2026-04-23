package com.seniorjuniorconnect.app.model

data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val seen: Boolean = false,
    val attachmentUrl: String = "",   // Firebase Storage URL if PDF
    val attachmentType: String = ""   // "pdf", "link", or ""
)