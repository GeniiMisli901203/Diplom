package com.example.ks1compose.models

data class RegistrationRequest(
    val email: String,
    val login: String,
    val password: String,
    val userName: String,
    val userSName: String,
    val userClass: String,
    val userSchool: String
)
