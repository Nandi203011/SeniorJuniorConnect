package com.seniorjuniorconnect.app.model

data class Question(
    val id: String = "",
    val question: String = "",
    val topic: String = "",
    val askedBy: String = "",
    val askedByUid: String = "",
    val answeredBy: String = "",
    val answer: String = "",
    val isAnswered: Boolean = false,
    val timestamp: Long = 0L
)