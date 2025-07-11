package com.example.finalthinksync

data class Summary(
    val id: String = "",
    val title: String = "",
    val course: String = "",
    val lecturer: String = "",
    val year: Int = 0,
    val timestamp: Long = 0L,
    val uploaderUid: String = "",
    val pdfUrl: String = "",
    var reviews: List<Review> = emptyList()

)
