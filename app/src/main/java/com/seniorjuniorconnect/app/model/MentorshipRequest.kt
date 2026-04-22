package com.seniorjuniorconnect.app.model

data class MentorshipRequest(
    val id: String = "",
    val juniorUid: String = "",
    val juniorName: String = "",
    val seniorUid: String = "",
    val seniorName: String = "",
    val message: String = "",
    val status: String = "pending", // pending, accepted, rejected
    val timestamp: Long = 0L
)