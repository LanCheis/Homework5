package com.example.homework5

data class PhotoEntity(
    val id: Long = 0,
    val uri: String,
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
