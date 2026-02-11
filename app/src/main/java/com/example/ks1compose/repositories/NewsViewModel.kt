package com.example.ks1compose.repositories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.NewsResponse
import com.example.ks1compose.models.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val _newsResponse = MutableStateFlow<NewsResponse?>(null)
    val newsResponse: StateFlow<NewsResponse?> = _newsResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun searchNews(query: String) {
        viewModelScope.launch {
            try {
                _newsResponse.value = null // Сброс предыдущего ответа
                _errorMessage.value = null // Сброс предыдущей ошибки
                val response = RetrofitInstance.apiService.searchNews(query)
                if (response.isSuccessful) {
                    _newsResponse.value = response.body()
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    fun deleteNews(newsId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.deleteNews(newsId)
                if (response.isSuccessful) {
                    _newsResponse.value = response.body()
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }
}
