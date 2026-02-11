package com.example.ks1compose.repository

import com.example.ks1compose.models.RegistrationRequest
import com.example.ks1compose.models.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegistrationRepository {
    suspend fun registerUser(request: RegistrationRequest): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.apiService.registerUser(request)
                if (response.isSuccessful) {
                    "Успешная регистрация"
                } else {
                    "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                "Ошибка сети: ${e.message}"
            }
        }
    }
}
