package com.example.ks1compose.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val userId: String,
    val email: String,
    val name: String,
    val sName: String,
    @SerializedName("class") val uClass: String,  // Важно: маппинг поля "class" из JSON
    val school: String,
    val role: String,
    val teacherId: String? = null
)

@Serializable
data class UserInformationResponse(
    val success: Boolean,
    val message: String? = null,
    val user: UserDTO? = null
)

@Serializable
data class UpdateUserRequest(
    val name: String,
    val sName: String,
    val uClass: String,
    val school: String
)

@Serializable
data class UsersListResponse(
    val success: Boolean,
    val message: String? = null,
    val students: List<UserDTO>? = null,
    val teachers: List<UserDTO>? = null
)