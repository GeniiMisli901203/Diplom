package com.example.ks1compose.repositories

import com.example.ks1compose.DTOs.GradeDTO
import com.example.ks1compose.DTOs.UpdateGradeRequest
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import com.example.ks1compose.models.TokenManager

class GradeRepository {
    private val api = RetrofitInstanceWithAuth.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    suspend fun addGrade(grade: GradeDTO): Result<String> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.addGrade("Bearer $token", grade)
            if (response.isSuccessful) {
                Result.Success("Оценка успешно добавлена")
            } else {
                Result.Error(response.message() ?: "Ошибка добавления оценки")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun getMyGrades(
        subject: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<com.example.ks1compose.models.GradeUIModel>> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.getMyGrades("Bearer $token", subject, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                val grades = response.body()!!.grades ?: emptyList()
                val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                Result.Success(uiModels)
            } else {
                Result.Error(response.message() ?: "Ошибка получения оценок")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun getClassGrades(className: String, subject: String? = null): Result<List<com.example.ks1compose.models.GradeUIModel>> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.getClassGrades("Bearer $token", className, subject)
            if (response.isSuccessful && response.body() != null) {
                val grades = response.body()!!.grades ?: emptyList()
                val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                Result.Success(uiModels)
            } else {
                Result.Error(response.message() ?: "Ошибка получения оценок класса")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun updateGrade(gradeId: String, gradeValue: Int, comment: String?): Result<String> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val request = UpdateGradeRequest(gradeValue, comment)
            val response = api.updateGrade("Bearer $token", gradeId, request)
            if (response.isSuccessful) {
                Result.Success("Оценка обновлена")
            } else {
                Result.Error(response.message() ?: "Ошибка обновления")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    suspend fun deleteGrade(gradeId: String): Result<String> {
        return try {
            val token = TokenManager.authToken ?: return Result.Error("Не авторизован")
            val response = api.deleteGrade("Bearer $token", gradeId)
            if (response.isSuccessful) {
                Result.Success("Оценка удалена")
            } else {
                Result.Error(response.message() ?: "Ошибка удаления")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Неизвестная ошибка")
        }
    }
}