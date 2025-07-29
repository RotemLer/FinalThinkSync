package com.example.finalthinksync

import com.google.firebase.Timestamp

data class Notification(
    val isRead: Boolean = false,
    val timestamp: Timestamp? = null,
    val data: Map<String, Any>? = null,
    val id: String = ""
)
