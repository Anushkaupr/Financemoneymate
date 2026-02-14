package com.example.financemoneymate.model

data class WishlistItem(
    val id: String = "",
    val wishName: String = "",
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
)