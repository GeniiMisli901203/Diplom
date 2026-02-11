package com.example.ks1compose.repositories

import com.example.ks1compose.DTOs.LessonDTO
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import com.example.ks1compose.models.TokenManager

class LessonRepository {
    private val api = RetrofitInstanceWithAuth.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun getLessonsByClassAndDay(
        className: String,
        dayOfWeek: String,
        weekNumber: Int? = null
    ): Result<List<com.example.ks1compose.models.LessonUIModel>> {
        return try {
            val response = api.getLessonsByClassAndDay(className, dayOfWeek, weekNumber)
            if (response.isSuccessful && response.body() != null) {
                val lessons = response.body()!!.lessons ?: emptyList()
                val uiModels = lessons.map { ModelConverter.convertLessonToUIModel(it) }
                Result.Success(uiModels)
            } else {
                Result.Error(response.message() ?: "Ошибка получения расписания")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun createLesson(lesson: LessonDTO): Result<String> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.createLesson("Bearer $token", lesson)
            if (response.isSuccessful) {
                Result.Success("Урок добавлен")
            } else {
                Result.Error(response.message() ?: "Ошибка добавления урока")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}