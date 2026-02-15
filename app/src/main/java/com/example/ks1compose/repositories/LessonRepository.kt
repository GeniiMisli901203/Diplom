package com.example.ks1compose.repositories

import com.example.ks1compose.DTOs.LessonDTO
import com.example.ks1compose.models.LessonUIModel
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LessonRepository {
    private val api = RetrofitInstance.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun getLessonsByClassAndDay(
        className: String,
        dayOfWeek: String,
        weekNumber: Int? = null
    ): Result<List<LessonUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getLessonsByClassAndDay(className, dayOfWeek, weekNumber)

                if (response.isSuccessful && response.body() != null) {
                    val lessons = response.body()!!.lessons ?: emptyList()
                    val uiModels = lessons.map { lesson ->
                        ModelConverter.convertLessonToUIModel(lesson)
                    }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка получения расписания", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun createLesson(lesson: LessonDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.createLesson("Bearer $token", lesson)

                if (response.isSuccessful) {
                    Result.Success("Урок успешно создан")
                } else {
                    Result.Error(response.message() ?: "Ошибка создания урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun updateLesson(lessonId: String, updates: Map<String, Any?>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.updateLesson("Bearer $token", lessonId, updates)

                if (response.isSuccessful) {
                    Result.Success("Урок обновлен")
                } else {
                    Result.Error(response.message() ?: "Ошибка обновления урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    suspend fun deleteLesson(lessonId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.deleteLesson("Bearer $token", lessonId)

                if (response.isSuccessful) {
                    Result.Success("Урок удален")
                } else {
                    Result.Error(response.message() ?: "Ошибка удаления урока", response.code())
                }
            } catch (e: HttpException) {
                Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }
}