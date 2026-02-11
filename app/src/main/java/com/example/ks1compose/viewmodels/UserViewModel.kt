package com.example.ks1compose.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.models.StudentUIModel
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.models.UserDTO
import com.example.ks1compose.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _userInfo = MutableStateFlow<UserDTO?>(null)
    val userInfo: StateFlow<UserDTO?> = _userInfo

    private val _students = MutableStateFlow<List<StudentUIModel>>(emptyList())
    val students: StateFlow<List<StudentUIModel>> = _students

    private val _teachers = MutableStateFlow<List<UserDTO>>(emptyList())
    val teachers: StateFlow<List<UserDTO>> = _teachers

    private val _updateResult = MutableStateFlow<UserRepository.Result<String>?>(null)
    val updateResult: StateFlow<UserRepository.Result<String>?> = _updateResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Загружаем информацию о пользователе при создании ViewModel
        if (TokenManager.authToken != null) {
            loadUserInfo()
        }
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
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
            _updateResult.value = repository.updateUserInfo(name, sName, uClass, school)
            if (_updateResult.value is UserRepository.Result.Success) {
                // Обновляем локальные данные
                loadUserInfo()
            }
            _isLoading.value = false
        }
    }

    fun loadStudentsByClass(className: String) {
        viewModelScope.launch {
            _isLoading.value = true
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