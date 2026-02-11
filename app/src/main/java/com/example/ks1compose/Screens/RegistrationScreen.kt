package com.example.ks1compose.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.models.RegistrationRequest
import com.example.ks1compose.repository.RegistrationRepository
import kotlinx.coroutines.launch

@Composable
fun RegistrationScreen(
    onNavigateToIdeas: (Any?) -> Unit,
    login: String
) {
    val coroutineScope = rememberCoroutineScope()
    val registrationRepository = remember { RegistrationRepository() }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userSName by remember { mutableStateOf("") }
    var userClass by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userSchool by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PersonalTextField(
            maxLines = 1,
            true,
            text = login,
            label = "Логин",
            15
        ) {
            login = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userName,
            label = "Имя",
            15
        ) {
            userName = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userSName,
            label = "Фамилия",
            15
        ) {
            userSName = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userClass,
            label = "Класс в формате (5А)",
            15
        ) {
            userClass = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userEmail,
            label = "Email",
            15
        ) {
            userEmail = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = password,
            label = "Пароль (минимум 8 символов)",
            15
        ) {
            password = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalTextField(
            maxLines = 1,
            true,
            text = userSchool,
            label = "Школа (полное название)",
            15
        ) {
            userSchool = it
        }

        Spacer(modifier = Modifier.height(10.dp))

        PersonalButton(text = "Зарегистрироваться") {

            if (login.isBlank() || userName.isBlank() || userSName.isBlank() ||
                userClass.isBlank() || userEmail.isBlank() || password.isBlank() ||
                userSchool.isBlank()) {
                error = "Все поля должны быть заполнены"
                return@PersonalButton
            }

            isLoading = true
            coroutineScope.launch {
                val result = registrationRepository.registerUser(
                    RegistrationRequest(
                        email = userEmail,
                        login = login,
                        password = password,
                        userName = userName,
                        userSName = userSName,
                        userClass = userClass,
                        userSchool = userSchool
                    )
                )
                isLoading = false
                if (result == "Успешная регистрация") {
                    onNavigateToIdeas(login)
                } else {
                    error = result
                }
            }
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                modifier = Modifier.padding(16.dp),
                color = Color.Red
            )
        }
    }
}

