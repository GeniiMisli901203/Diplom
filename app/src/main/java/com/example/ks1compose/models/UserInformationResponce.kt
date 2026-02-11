package com.example.ks1compose.models

data class UserInformationResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

data class User(
    val userId: String,
    val email: String,
    val name: String,
    val sName: String,
    val uClass: String,
    val school: String
)

data class UserInfoRequest(
    val name: String,
    val sName: String,
    val uClass: String,
    val school: String
)
