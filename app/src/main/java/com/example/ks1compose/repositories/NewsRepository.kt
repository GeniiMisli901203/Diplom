package com.example.ks1compose.repositories

import android.content.Context
import com.example.ks1compose.DTOs.NewsDTO
import com.example.ks1compose.DTOs.NewsResponse
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NewsRepository(private val context: Context) {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("news_cache", Context.MODE_PRIVATE)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
        object Loading : Result<Nothing>()
    }

    // ================ ОСНОВНЫЕ МЕТОДЫ ================

    // Получить все новости
    suspend fun getAllNews(): Response<NewsResponse> {
        return try {
            api.getAllNews()
        } catch (e: Exception) {
            throw e
        }
    }

    // Получить все новости с кэшированием
    suspend fun getAllNewsCached(): Result<List<NewsDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getAllNews()
                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()!!.newsList ?: emptyList()
                    saveNewsToCache(newsList)
                    Result.Success(newsList)
                } else {
                    getNewsFromCache()?.let {
                        Result.Success(it)
                    } ?: Result.Error("Нет данных и нет кэша")
                }
            } catch (e: HttpException) {
                getNewsFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Ошибка сети: ${e.message()}", e.code())
            } catch (e: IOException) {
                getNewsFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Проверьте подключение к интернету")
            } catch (e: Exception) {
                getNewsFromCache()?.let {
                    Result.Success(it)
                } ?: Result.Error("Неизвестная ошибка: ${e.message}")
            }
        }
    }

    // Добавить новость
    suspend fun addNews(token: String, news: NewsDTO): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiWithAuth.addNews(token, news)
                if (response.isSuccessful && response.body() != null) {
                    clearNewsCache()
                    Result.Success("Новость успешно добавлена")
                } else {
                    Result.Error(
                        response.message() ?: "Ошибка добавления новости",
                        response.code()
                    )
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

    // Поиск новостей
    suspend fun searchNews(query: String): Response<NewsResponse> {
        return try {
            api.searchNews(query)
        } catch (e: Exception) {
            throw e
        }
    }

    // Поиск новостей с кэшированием
    suspend fun searchNewsCached(query: String): Result<List<NewsDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.searchNews(query)
                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()!!.newsList ?: emptyList()
                    Result.Success(newsList)
                } else {
                    Result.Error("Новости не найдены", response.code())
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

    // Удалить новость
    suspend fun deleteNews(token: String, newsId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiWithAuth.deleteNews(token, newsId)
                if (response.isSuccessful) {
                    clearNewsCache()
                    Result.Success("Новость успешно удалена")
                } else {
                    Result.Error(
                        response.message() ?: "Ошибка удаления новости",
                        response.code()
                    )
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

    // ================ МЕТОДЫ ДЛЯ РАБОТЫ С КЭШЕМ ================

    private fun saveNewsToCache(newsList: List<NewsDTO>) {
        try {
            val json = gson.toJson(newsList)
            prefs.edit().putString("cached_news", json).apply()
            prefs.edit().putLong("cache_time", System.currentTimeMillis()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getNewsFromCache(): List<NewsDTO>? {
        return try {
            val json = prefs.getString("cached_news", null)
            val cacheTime = prefs.getLong("cache_time", 0)
            val isExpired = System.currentTimeMillis() - cacheTime > 10 * 60 * 1000 // 10 минут

            if (json != null && !isExpired) {
                val type = object : TypeToken<List<NewsDTO>>() {}.type
                gson.fromJson<List<NewsDTO>>(json, type)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun clearNewsCache() {
        prefs.edit().remove("cached_news").remove("cache_time").apply()
    }
}