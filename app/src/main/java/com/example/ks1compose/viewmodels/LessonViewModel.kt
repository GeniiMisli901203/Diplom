package com.example.ks1compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.LessonDTO
import com.example.ks1compose.models.LessonUIModel
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.repositories.LessonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class LessonViewModel : ViewModel() {
    private val repository = LessonRepository()

    // Используем MutableStateFlow с явным указанием типа
    private val _todayLessons = MutableStateFlow<List<LessonUIModel>>(emptyList())
    val todayLessons: StateFlow<List<LessonUIModel>> = _todayLessons.asStateFlow()

    private val _weeklySchedule = MutableStateFlow<Map<String, List<LessonUIModel>>?>(null)
    val weeklySchedule: StateFlow<Map<String, List<LessonUIModel>>?> = _weeklySchedule.asStateFlow()

    private val _createLessonResult = MutableStateFlow<LessonRepository.Result<String>?>(null)
    val createLessonResult: StateFlow<LessonRepository.Result<String>?> = _createLessonResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Явно указываем тип String? для errorMessage
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Загрузка расписания на сегодня
    fun loadTodayLessons(className: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val dayOfWeek = getCurrentDayOfWeek()

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek)) {
                is LessonRepository.Result.Success -> {
                    _todayLessons.value = result.data
                    _errorMessage.value = null
                }
                is LessonRepository.Result.Error -> {
                    _errorMessage.value = result.message
                    _todayLessons.value = emptyList()
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
            _errorMessage.value = null

            when (val result = repository.getLessonsByClassAndDay(className, dayOfWeek, weekNumber)) {
                is LessonRepository.Result.Success -> {
                    if (dayOfWeek == getCurrentDayOfWeek()) {
                        _todayLessons.value = result.data
                    }
                    _errorMessage.value = null
                }
                is LessonRepository.Result.Error -> {
                    _errorMessage.value = result.message
                    if (dayOfWeek == getCurrentDayOfWeek()) {
                        _todayLessons.value = emptyList()
                    }
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
            _errorMessage.value = null

            val teacherId = TokenManager.userId ?: run {
                _errorMessage.value = "ID учителя не найден"
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
        _errorMessage.value = null
    }

    // Вспомогательная функция для получения текущего дня недели
    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            Calendar.SUNDAY -> "sunday"
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