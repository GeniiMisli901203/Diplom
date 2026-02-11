package com.example.ks1compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.LessonDTO
import com.example.ks1compose.DTOs.WeeklyScheduleDTO
import com.example.ks1compose.models.LessonUIModel
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.repositories.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LessonViewModel : ViewModel() {
    private val repository = LessonRepository()

    private val _todayLessons = MutableStateFlow<List<LessonUIModel>>(emptyList())
    val todayLessons: StateFlow<List<LessonUIModel>> = _todayLessons

    private val _weeklySchedule = MutableStateFlow<WeeklyScheduleDTO?>(null)
    val weeklySchedule: StateFlow<WeeklyScheduleDTO?> = _weeklySchedule

    private val _createLessonResult = MutableStateFlow<LessonRepository.Result<String>?>(null)
    val createLessonResult: StateFlow<LessonRepository.Result<String>?> = _createLessonResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Загрузка расписания на сегодня
    fun loadTodayLessons(className: String) {
        viewModelScope.launch {
            _isLoading.value = true

            // Получаем текущий день недели на английском
            val dayOfWeek = getCurrentDayOfWeek()

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek)) {
                is LessonRepository.Result.Success -> {
                    _todayLessons.value = result.data
                    _error.value = null
                }
                is LessonRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Загрузка расписания на конкретный день
    fun loadLessonsByDay(className: String, dayOfWeek: String, weekNumber: Int? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek, weekNumber)) {
                is LessonRepository.Result.Success -> {
                    if (dayOfWeek == getCurrentDayOfWeek()) {
                        _todayLessons.value = result.data
                    }
                    _error.value = null
                }
                is LessonRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Создание урока (учитель)
    fun createLesson(
        className: String,
        dayOfWeek: String,
        lessonNumber: Int,
        subjectName: String,
        room: String? = null,
        startTime: String? = null,
        endTime: String? = null,
        weekNumber: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val teacherId = TokenManager.userId ?: run {
                _error.value = "ID учителя не найден"
                _isLoading.value = false
                return@launch
            }

            val lesson = LessonDTO(
                className = className,
                dayOfWeek = dayOfWeek,
                weekNumber = weekNumber,
                lessonNumber = lessonNumber,
                subjectName = subjectName,
                teacherId = teacherId,
                room = room,
                startTime = startTime,
                endTime = endTime
            )

            _createLessonResult.value = repository.createLesson(lesson)
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _createLessonResult.value = null
    }

    fun clearError() {
        _error.value = null
    }

    // Вспомогательная функция для получения текущего дня недели
    private fun getCurrentDayOfWeek(): String {
        val calendar = java.util.Calendar.getInstance()
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "monday"
            java.util.Calendar.TUESDAY -> "tuesday"
            java.util.Calendar.WEDNESDAY -> "wednesday"
            java.util.Calendar.THURSDAY -> "thursday"
            java.util.Calendar.FRIDAY -> "friday"
            java.util.Calendar.SATURDAY -> "saturday"
            java.util.Calendar.SUNDAY -> "sunday"
            else -> "monday"
        }
    }

    companion object {
        val daysOfWeek = listOf(
            "monday" to "Понедельник",
            "tuesday" to "Вторник",
            "wednesday" to "Среда",
            "thursday" to "Четверг",
            "friday" to "Пятница",
            "saturday" to "Суббота",
            "sunday" to "Воскресенье"
        )

        fun getRussianDayName(englishDay: String): String {
            return daysOfWeek.find { it.first == englishDay }?.second ?: englishDay
        }
    }
}