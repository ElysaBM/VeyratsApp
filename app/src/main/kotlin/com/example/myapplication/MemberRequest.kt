package com.example.myapplication

data class MemberRequest(
    val userId: String,
    val role: String,
    val custom: Map<String, Any>
)