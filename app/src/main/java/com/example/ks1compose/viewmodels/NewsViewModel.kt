package com.example.ks1compose.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.NewsDTO
import com.example.ks1compose.DTOs.NewsResponse
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class NewsViewModel : ViewModel() {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService

    private val _allNews = MutableStateFlow<List<NewsDTO>>(emptyList())
    val allNews: StateFlow<List<NewsDTO>> = _allNews

    private val _newsResponse = MutableStateFlow<NewsResponse?>(null)
    val newsResponse: StateFlow<NewsResponse?> = _newsResponse

    private val _addNewsResult = MutableStateFlow<String?>(null)
    val addNewsResult: StateFlow<String?> = _addNewsResult

    private val _deleteNewsResult = MutableStateFlow<String?>(null)
    val deleteNewsResult: StateFlow<String?> = _deleteNewsResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadAllNews()
    }

    fun loadAllNews() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getAllNews()
                if (response.isSuccessful) {
                    _allNews.value = response.body()?.newsList ?: emptyList()
                    _newsResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addNews(token: String, title: String, description: String, url: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            val userId = com.example.ks1compose.models.TokenManager.userId ?: run {
                _errorMessage.value = "Не авторизован"
                _isLoading.value = false
                return@launch
            }

            val news = NewsDTO(
                userId = userId,
                title = title,
                description = description,
                url = url
            )

            try {
                val response = apiWithAuth.addNews(token, news)
                if (response.isSuccessful) {
                    _addNewsResult.value = "Новость успешно добавлена"
                    loadAllNews() // Перезагружаем список
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchNews(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.searchNews(query)
                if (response.isSuccessful) {
                    _allNews.value = response.body()?.newsList ?: emptyList()
                    _newsResponse.value = response.body()
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNews(token: String, newsId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiWithAuth.deleteNews(token, newsId)
                if (response.isSuccessful) {
                    _deleteNewsResult.value = "Новость успешно удалена"
                    loadAllNews() // Перезагружаем список
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearResults() {
        _addNewsResult.value = null
        _deleteNewsResult.value = null
        _errorMessage.value = null
    }
}