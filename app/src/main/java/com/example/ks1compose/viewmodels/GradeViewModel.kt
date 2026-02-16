package com.example.ks1compose.viewmodels

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.DTOs.GradeDTO
import com.example.ks1compose.models.GradeUIModel
import com.example.ks1compose.models.ModelConverter
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.repositories.GradeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GradeViewModel : ViewModel() {
    private val repository = GradeRepository()

    private val _myGrades = MutableStateFlow<List<GradeUIModel>>(emptyList())
    val myGrades: StateFlow<List<GradeUIModel>> = _myGrades.asStateFlow()

    private val _classGrades = MutableStateFlow<List<GradeUIModel>>(emptyList())
    val classGrades: StateFlow<List<GradeUIModel>> = _classGrades.asStateFlow()

    private val _averageGrade = MutableStateFlow<Double?>(null)
    val averageGrade: StateFlow<Double?> = _averageGrade.asStateFlow()

    private val _addGradeResult = MutableStateFlow<GradeRepository.Result<String>?>(null)
    val addGradeResult: StateFlow<GradeRepository.Result<String>?> = _addGradeResult.asStateFlow()

    private val _updateGradeResult = MutableStateFlow<GradeRepository.Result<String>?>(null)
    val updateGradeResult: StateFlow<GradeRepository.Result<String>?> = _updateGradeResult.asStateFlow()

    private val _deleteGradeResult = MutableStateFlow<GradeRepository.Result<String>?>(null)
    val deleteGradeResult: StateFlow<GradeRepository.Result<String>?> = _deleteGradeResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Загрузка оценок ученика
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadMyGrades(
        subject: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getMyGrades(subject, startDate, endDate)) {
                is GradeRepository.Result.Success -> {
                    _myGrades.value = result.data
                    // Вычисляем средний балл
                    _averageGrade.value = if (result.data.isNotEmpty()) {
                        result.data.map { it.gradeValue }.average()
                    } else {
                        null
                    }
                }
                is GradeRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Загрузка оценок за сегодня (для ученика)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun loadTodayGrades() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val studentId = TokenManager.userId ?: run {
                _error.value = "Не авторизован"
                _isLoading.value = false
                return@launch
            }

            when (val result = repository.getTodayGrades(studentId)) {
                is GradeRepository.Result.Success -> {
                    _myGrades.value = result.data
                    _averageGrade.value = if (result.data.isNotEmpty()) {
                        result.data.map { it.gradeValue }.average()
                    } else {
                        null
                    }
                }
                is GradeRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadUserGrades(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getUserGrades(userId)) {
                is GradeRepository.Result.Success -> {
                    _myGrades.value = result.data
                    _averageGrade.value = if (result.data.isNotEmpty()) {
                        result.data.map { it.gradeValue }.average()
                    } else {
                        null
                    }
                }
                is GradeRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }


    // Загрузка оценок класса (для учителя)
    fun loadClassGrades(className: String, subject: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getClassGrades(className, subject)) {
                is GradeRepository.Result.Success -> {
                    _classGrades.value = result.data
                }
                is GradeRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Добавление оценки (учитель)
    fun addGrade(
        studentId: String,
        subjectName: String,
        className: String,
        gradeValue: Int,
        gradeType: String,
        comment: String? = null,
        lessonDate: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val teacherId = TokenManager.userId ?: run {
                _error.value = "ID учителя не найден"
                _isLoading.value = false
                return@launch
            }

            val grade = GradeDTO(
                studentId = studentId,
                teacherId = teacherId,
                subjectName = subjectName,
                className = className,
                gradeValue = gradeValue,
                gradeType = gradeType,
                comment = comment,
                lessonDate = lessonDate
            )

            _addGradeResult.value = repository.addGrade(grade)
            _isLoading.value = false
        }
    }

    // Обновление оценки
    fun updateGrade(gradeId: String, gradeValue: Int, comment: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateGradeResult.value = repository.updateGrade(gradeId, gradeValue, comment)
            _isLoading.value = false
        }
    }

    // Удаление оценки
    fun deleteGrade(gradeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _deleteGradeResult.value = repository.deleteGrade(gradeId)
            _isLoading.value = false
        }
    }

    fun clearResults() {
        _addGradeResult.value = null
        _updateGradeResult.value = null
        _deleteGradeResult.value = null
    }

    fun clearError() {
        _error.value = null
    }

    // Получить цвет для оценки
    fun getGradeColor(gradeValue: Int): androidx.compose.ui.graphics.Color {
        return when (gradeValue) {
            5 -> androidx.compose.ui.graphics.Color.Green
            4 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
            3 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
            2 -> androidx.compose.ui.graphics.Color.Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }
    }
}