package com.example.ks1compose.Screens

import UserRepository
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ks1compose.models.UserInformationResponse
import com.example.ks1compose.ui.theme.DarkPink
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    onAddSchedule: () -> Unit,
    onAddIdea: () -> Unit,
    onViewAllSchedules: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    userLogin: String,
    token: String,
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    var userInfo by remember { mutableStateOf<UserInformationResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchUserInfo() {
        isLoading = true
        coroutineScope.launch {
            try {
                userInfo = userRepository.getUserInfo(userLogin)
                if (userInfo?.success == true && userInfo?.user != null) {

                } else {
                    errorMessage = "Ошибка загрузки данных: ${userInfo?.message}"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки данных"
            }
            isLoading = false
        }
    }

    LaunchedEffect(userLogin) {
        fetchUserInfo()
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(start = 24.dp, bottom = 8.dp, top = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.End
    ) {
        // Переключатель тем
        Switch(
            checked = darkTheme,
            onCheckedChange = onThemeChange,
            modifier = Modifier.padding(16.dp)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator()

        } else if (errorMessage != null) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { fetchUserInfo() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Обновить")
                }
            }
        } else if (userInfo?.user == null) {
            Text(
                text = "Нет данных пользователя",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            userInfo?.let { info ->
                if (info.success && info.user != null) {
                    Text(
                        text = "Вы зашли как ${info.user.name} ${info.user.sName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Имя: ${info.user.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Фамилия: ${info.user.sName}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Класс: ${info.user.uClass}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Школа: ${info.user.school}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Email: ${info.user.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            PersonalButton(
                text = "Редактировать профиль",
                style = MaterialTheme.typography.bodyMedium
            ) {
                onEditProfile()
            }

            if (userInfo?.user?.email == "m.znakin@mail.ru") {
                Spacer(modifier = Modifier.height(16.dp))
                PersonalButton(text = "Добавить новость", style = MaterialTheme.typography.bodyMedium) {
                    onAddIdea()
                }
                Spacer(modifier = Modifier.height(16.dp))
                PersonalButton(
                    text = "Просмотреть все расписания",
                    style = MaterialTheme.typography.bodyMedium
                ) {
                    onViewAllSchedules()
                }
                Spacer(modifier = Modifier.height(16.dp))
                PersonalButton(
                    text = "Добавить расписание",
                    style = MaterialTheme.typography.bodyMedium
                ) {
                    onAddSchedule()
                }
            }
        }
        LogoutButton(text = "Выйти из аккаунта") {
            onLogout()
        }
    }
}

@Composable
fun PersonalButton(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text, style = style)
    }
}

@Composable
fun LogoutButton( text: String,
                  onClick: () -> Unit ){
    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkPink,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text)
    }
}
