package com.example.ks1compose.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ks1compose.models.RegistrationRequest
import com.example.ks1compose.models.TokenResponse
import com.example.ks1compose.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginResult = MutableStateFlow<AuthRepository.Result<TokenResponse>?>(null)
    val loginResult: StateFlow<AuthRepository.Result<TokenResponse>?> = _loginResult

    private val _registerResult = MutableStateFlow<AuthRepository.Result<TokenResponse>?>(null)
    val registerResult: StateFlow<AuthRepository.Result<TokenResponse>?> = _registerResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = repository.login(login, password)
            _isLoading.value = false
        }
    }

    fun register(
        login: String,
        email: String,
        password: String,
        userName: String,
        userSName: String,
        userClass: String,
        userSchool: String,
        role: String = "student"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val request = RegistrationRequest(
                login = login,
                email = email,
                password = password,
                userName = userName,
                userSName = userSName,
                userClass = userClass,
                userSchool = userSchool,
                role = role
            )
            _registerResult.value = repository.register(request)
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _loginResult.value = null
        _registerResult.value = null
    }

    fun resetResults() {
        _loginResult.value = null
        _registerResult.value = null
    }
}