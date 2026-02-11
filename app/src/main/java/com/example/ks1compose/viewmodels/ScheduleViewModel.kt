package com.example.ks1compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.ScheduleDTO
import com.example.ks1compose.DTOs.ScheduleResponse
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.models.RetrofitInstanceWithAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URLEncoder

class ScheduleViewModel : ViewModel() {
    private val api = RetrofitInstance.apiService
    private val apiWithAuth = RetrofitInstanceWithAuth.apiService

    private val _scheduleResponse = MutableStateFlow<ScheduleResponse?>(null)
    val scheduleResponse: StateFlow<ScheduleResponse?> = _scheduleResponse

    private val _allSchedules = MutableStateFlow<List<ScheduleDTO>>(emptyList())
    val allSchedules: StateFlow<List<ScheduleDTO>> = _allSchedules

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult

    fun loadAllSchedules() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getAllSchedules()
                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
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

    fun addSchedule(token: String, schedule: ScheduleDTO) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiWithAuth.addSchedule(token, schedule)
                if (response.isSuccessful) {
                    _operationResult.value = "Расписание успешно добавлено"
                    loadAllSchedules() // Перезагружаем список
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSchedule(className: String, day: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val encodedClassName = URLEncoder.encode(className, "UTF-8")
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                val response = api.getSchedule(encodedClassName, encodedDay)

                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
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

    fun getSchedulesByDay(day: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                val response = api.getSchedulesByDay(encodedDay)

                if (response.isSuccessful) {
                    _allSchedules.value = response.body()?.schedules ?: emptyList()
                    _scheduleResponse.value = response.body()
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

    fun deleteSchedule(token: String, scheduleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiWithAuth.deleteSchedule(token, scheduleId)
                if (response.isSuccessful) {
                    _operationResult.value = "Расписание удалено"
                    loadAllSchedules() // Перезагружаем список
                } else {
                    _errorMessage.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Проверьте подключение к интернету"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _operationResult.value = null
    }
}