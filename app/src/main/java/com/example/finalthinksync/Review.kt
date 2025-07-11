package com.example.finalthinksync

data class Review(
    val userId: String = "",
    val username: String = "",
    val rating: Int = 0,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var reply: String? = null

)
