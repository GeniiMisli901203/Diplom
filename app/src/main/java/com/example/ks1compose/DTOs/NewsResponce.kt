package com.example.ks1compose.DTOs


import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    val success: Boolean,
    val message: String? = null,
    val newsList: List<NewsDTO>? = null
)
