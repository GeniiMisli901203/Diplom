package com.example.ks1compose.DTOs

import com.example.ks1compose.models.UserDTO
import kotlinx.serialization.Serializable

@Serializable
data class StudentsListResponse(
    val success: Boolean,
    val message: String? = null,
    val students: List<UserDTO>? = null
)