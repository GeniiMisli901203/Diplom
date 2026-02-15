package com.example.ks1compose.repositories

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.ks1compose.DTOs.GradeDTO
import com.example.ks1compose.DTOs.UpdateGradeRequest
import com.example.ks1compose.models.GradeUIModel
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import com.example.ks1compose.models.TokenManager
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class GradeRepository {
    private val api = RetrofitInstanceWithAuth.apiService

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    // Получить оценки ученика за сегодня
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun getTodayGrades(studentId: String): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getTodayGrades(studentId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    // Получить оценки ученика
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun getMyGrades(
        subject: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getMyGrades("Bearer $token", subject, startDate, endDate)

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    // Получить оценки класса
    suspend fun getClassGrades(className: String, subject: String? = null): Result<List<GradeUIModel>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.getClassGrades("Bearer $token", className, subject)

                if (response.isSuccessful && response.body() != null) {
                    val grades = response.body()!!.grades ?: emptyList()
                    val uiModels = grades.map { ModelConverter.convertGradeToUIModel(it) }
                    Result.Success(uiModels)
                } else {
                    Result.Error(response.message() ?: "Ошибка загрузки", response.code())
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

    // Добавить оценку
    suspend fun addGrade(grade: GradeDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.addGrade("Bearer $token", grade)

                if (response.isSuccessful) {
                    Result.Success("Оценка успешно добавлена")
                } else {
                    Result.Error(response.message() ?: "Ошибка добавления", response.code())
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

    // Обновить оценку
    suspend fun updateGrade(gradeId: String, gradeValue: Int, comment: String?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val request = UpdateGradeRequest(gradeValue, comment)
                val response = api.updateGrade("Bearer $token", gradeId, request)

                if (response.isSuccessful) {
                    Result.Success("Оценка обновлена")
                } else {
                    Result.Error(response.message() ?: "Ошибка обновления", response.code())
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

    // Удалить оценку
    suspend fun deleteGrade(gradeId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = TokenManager.authToken ?: return@withContext Result.Error("Не авторизован")
                val response = api.deleteGrade("Bearer $token", gradeId)

                if (response.isSuccessful) {
                    Result.Success("Оценка удалена")
                } else {
                    Result.Error(response.message() ?: "Ошибка удаления", response.code())
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