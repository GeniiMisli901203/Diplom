package com.example.ks1compose.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.R
import com.example.ks1compose.models.LoginRequest
import com.example.ks1compose.models.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToIdeas: (String, String) -> Unit,
    onNavigateToRegistration: () -> Unit,
    token: String
) {
    val coroutineScope = rememberCoroutineScope()
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(R.drawable.logo4), contentDescription = "logo")

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            "Расписание школы!!!",
            modifier = Modifier
                .drawWithCache {
                    val brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF9E82F0),
                            Color(0xFF42A5F5)
                        )
                    )
                    onDrawBehind {
                        drawRoundRect(
                            brush,
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                    }
                }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userName,
            label = "Логин",
            15
        ) {
            userName = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            1,
            true,
            password,
            "Пароль",
            15
        ) {
            password = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalButton(text = "Войти") {
            if (userName.isBlank() || password.isBlank()) {
                error = "Все поля должны быть заполнены"
                return@PersonalButton
            }

            isLoading = true
            error = null
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.apiService.loginUser(
                        LoginRequest(
                            login = userName,
                            password = password
                        )
                    )

                    if (response.isSuccessful) {
                        response.body()?.let { tokenResponse ->
                            if (tokenResponse.token.isNotEmpty()) {
                                onNavigateToIdeas(tokenResponse.token, userName) // Передайте токен и логин
                            } else {
                                error = "Ошибка входа"
                            }
                        } ?: run {
                            error = "Пустой ответ от сервера"
                        }
                    } else {
                        error = "Ошибка: ${response.code()}"
                    }
                } catch (e: Exception) {
                    error = "Ошибка сети: ${e.localizedMessage}"
                    Log.e("LoginScreen", "Login error", e)
                } finally {
                    isLoading = false
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalButton(text = "Регистрация") {
            onNavigateToRegistration()
        }

        if (error != null) {
            Text(
                text = error!!,
                modifier = Modifier.padding(16.dp),
                color = Color.Red
            )
        }
    }
}
