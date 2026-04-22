package com.seniorjuniorconnect.app.model

data class Senior(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val college: String = "",
    val branch: String = "",
    val year: String = "",
    val bio: String = "",
    val linkedIn: String = "",
    val domains: List<String> = emptyList(),
    val subjects: String = "",
    val placedAt: String = "",
    val finalRound: String = "",
    val interviewRound: String = ""
)