
package com.example.ks1compose.repositories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.ScheduleDTO
import com.example.ks1compose.DTOs.ScheduleResponse
import com.example.ks1compose.models.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.net.URLEncoder

class ScheduleViewModel : ViewModel() {
    private val _scheduleResponse = MutableStateFlow<ScheduleResponse?>(null)
    val scheduleResponse: StateFlow<ScheduleResponse?> = _scheduleResponse

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addSchedule(schedule: ScheduleDTO) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.addSchedule(schedule)
                handleResponse(response)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    fun getSchedule(className: String, day: String) {
        viewModelScope.launch {
            try {
                val encodedClassName = URLEncoder.encode(className, "UTF-8")
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                val response = RetrofitInstance.apiService.getSchedule(encodedClassName, encodedDay)
                handleResponse(response)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    fun getSchedulesByDay(day: String) {
        viewModelScope.launch {
            try {
                val encodedDay = URLEncoder.encode(day, "UTF-8")
                Log.d("ScheduleViewModel", "Encoded day: $encodedDay")
                val response = RetrofitInstance.apiService.getSchedulesByDay(encodedDay)
                handleResponse(response)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сети: ${e.message}"
            }
        }
    }


    private fun handleResponse(response: Response<ScheduleResponse>) {
        if (response.isSuccessful) {
            _scheduleResponse.value = response.body()
        } else {
            _errorMessage.value = "Ошибка сервера: ${response.code()}"
        }
    }
}
