// com.example.ks1compose.repositories.UserRepository.kt
package com.example.ks1compose.repositories

import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import com.example.ks1compose.models.StudentUIModel
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.models.UpdateUserRequest
import com.example.ks1compose.models.UserDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UserRepository {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()  // Только message
        object Loading : Result<Nothing>()
    }

    suspend fun getUserInfo(): Result<UserDTO> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.getUserInfoByToken("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user
                if (user != null) {
                    Result.Success(user)
                } else {
                    Result.Error("Пользователь не найден")
                }
            } else {
                Result.Error(response.message() ?: "Ошибка получения информации")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    // com.example.ks1compose.repositories.UserRepository.kt
    // com.example.ks1compose.repositories.UserRepository.kt
    suspend fun updateUserInfo(
        userId: String,
        name: String,
        sName: String,
        uClass: String,
        school: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val request = UpdateUserRequest(name, sName, uClass, school)

                println("📤 Sending update for user $userId to: /user/update/$userId")
                val response = api.updateUserById(userId, "Bearer $token", request)

                if (response.isSuccessful) {
                    println("✅ Update successful: ${response.body()}")
                    Result.Success("Данные обновлены")
                } else {
                    println("❌ Update failed: ${response.code()} - ${response.message()}")
                    Result.Error(response.message() ?: "Ошибка обновления")
                }
            } catch (e: Exception) {
                println("🔥 Exception: ${e.message}")
                Result.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    suspend fun getStudentsByClass(className: String): Result<List<StudentUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getStudentsByClass(className)

                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!.students ?: emptyList()
                    val uiModels = students.map { user ->
                        StudentUIModel(
                            id = user.userId,
                            name = "${user.name ?: ""} ${user.sName ?: ""}".trim(),
                            className = user.uClass
                        )
                    }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки учеников")
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}")
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getAllTeachers(): Result<List<UserDTO>> {
        return try {
            val response = api.getAllTeachers()
            if (response.isSuccessful && response.body() != null) {
                val teachers = response.body()!!.teachers ?: emptyList()
                Result.Success(teachers)
            } else {
                Result.Error(response.message() ?: "Ошибка получения учителей")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }
    // com.example.ks1compose.repositories.UserRepository.kt
// Убедитесь, что метод getAllStudents выглядит так:

    suspend fun getAllStudents(): Result<List<UserDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                println("📡 Запрос к /students/all")
                val response = api.getAllStudents() // Это должно быть /students/all

                println("📡 Статус: ${response.code()}")
                println("📡 Ответ: ${response.body()}")

                if (response.isSuccessful && response.body() != null) {
                    val students = response.body()!!.students ?: emptyList()
                    println("📡 Получено учеников: ${students.size}")
                    Result.Success(students)
                } else {
                    println("📡 Ошибка: ${response.message()}")
                    Result.Error(response.message() ?: "Ошибка загрузки учеников")
                }
            } catch (e: HttpException) {
                println("📡 HttpException: ${e.message()}")
                Result.Error("Ошибка сети: ${e.message()}")
            } catch (e: IOException) {
                println("📡 IOException: ${e.message}")
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                println("📡 Exception: ${e.message}")
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun getUserById(userId: String): Result<UserDTO> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getUserById(userId)
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.user
                    if (user != null) {
                        Result.Success(user)
                    } else {
                        Result.Error("Пользователь не найден")
                    }
                } else {
                    Result.Error(response.message() ?: "Ошибка получения пользователя")
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}