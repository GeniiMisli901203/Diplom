package com.example.ks1compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.models.StudentUIModel
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.models.UserDTO
import com.example.ks1compose.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    // Состояния для информации о пользователе
    private val _userInfo = MutableStateFlow<UserDTO?>(null)
    val userInfo: StateFlow<UserDTO?> = _userInfo.asStateFlow()

    private val _students = MutableStateFlow<List<StudentUIModel>>(emptyList())
    val students: StateFlow<List<StudentUIModel>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<List<UserDTO>>(emptyList())
    val teachers: StateFlow<List<UserDTO>> = _teachers.asStateFlow()

    // Состояние для результата обновления
    sealed class UpdateResult {
        object Success : UpdateResult()
        data class Error(val message: String) : UpdateResult()
        object Loading : UpdateResult()
    }

    private val _updateResult = MutableStateFlow<UpdateResult?>(null)
    val updateResult: StateFlow<UpdateResult?> = _updateResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Загружаем информацию о пользователе при создании ViewModel
        if (TokenManager.authToken != null) {
            loadUserInfo()
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getUserInfo()) {
                is UserRepository.Result.Success -> {
                    _userInfo.value = result.data
                    // Обновляем TokenManager
                    TokenManager.userId = result.data.userId
                    TokenManager.userRole = result.data.role
                    TokenManager.userName = result.data.name
                    TokenManager.userSName = result.data.sName
                    _error.value = null
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateUserInfo(
        name: String,
        sName: String,
        uClass: String,
        school: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _updateResult.value = UpdateResult.Loading
            _error.value = null

            when (val result = repository.updateUserInfo(name, sName, uClass, school)) {
                is UserRepository.Result.Success -> {
                    _updateResult.value = UpdateResult.Success
                    // Обновляем локальные данные
                    loadUserInfo()
                }
                is UserRepository.Result.Error -> {
                    _updateResult.value = UpdateResult.Error(result.message)
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadStudentsByClass(className: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getStudentsByClass(className)) {
                is UserRepository.Result.Success -> {
                    _students.value = result.data
                    _error.value = null
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadAllTeachers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getAllTeachers()) {
                is UserRepository.Result.Success -> {
                    _teachers.value = result.data
                    _error.value = null
                }
                is UserRepository.Result.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }

    fun clearError() {
        _error.value = null
    }

    val isTeacher: Boolean
        get() = _userInfo.value?.role == "teacher"

    val isStudent: Boolean
        get() = _userInfo.value?.role == "student"

    val isAdmin: Boolean
        get() = _userInfo.value?.role == "admin"

    val fullName: String
        get() = _userInfo.value?.let { "${it.name} ${it.sName}" } ?: ""
}