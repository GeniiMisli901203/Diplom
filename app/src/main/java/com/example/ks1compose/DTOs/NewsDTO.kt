package com.example.ks1compose.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class NewsDTO(
    val userId: String,
    val title: String,
    val description: String,
    val url: String
)
