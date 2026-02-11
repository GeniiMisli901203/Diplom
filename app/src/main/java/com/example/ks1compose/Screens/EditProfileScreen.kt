package com.example.ks1compose.Screens

import UserRepository
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.viewmodels.UserViewModel
import com.example.ks1compose.ui.theme.LightGrey
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    onProfileUpdated: () -> Unit,
    userLogin: String
) {
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel()

    var userName by rememberSaveable { mutableStateOf("") }
    var userSName by rememberSaveable { mutableStateOf("") }
    var userClass by rememberSaveable { mutableStateOf("") }
    var userSchool by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var isUpdating by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userLogin) {
        try {
            isLoading = true
            val userInfo = userRepository.getUserInfo(userLogin)

            if (userInfo.success && userInfo.user != null) {
                userName = userInfo.user.name
                userSName = userInfo.user.sName
                userClass = userInfo.user.uClass
                userSchool = userInfo.user.school
            } else {
                errorMessage = userInfo.message ?: "Не удалось загрузить данные пользователя"
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка загрузки: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = LightGrey)
        } else {
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colors.error)
                Spacer(modifier = Modifier.height(16.dp))
            }

            PersonalTextField(
                maxLines = 1,
                singleLine = true,
                userName,
                label = "Имя",
                padding = 8,
                onValueChange = { userName = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            PersonalTextField(
                maxLines = 1,
                singleLine = true,
                userSName,
                label = "Фамилия",
                padding = 8,
                onValueChange = { userSName = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            PersonalTextField(
                maxLines = 1,
                singleLine = true,
                userClass,
                label = "Класс",
                padding = 8,
                onValueChange = { userClass = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            PersonalTextField(
                maxLines = 1,
                singleLine = true,
                userSchool,
                label = "Школа",
                padding = 8,
                onValueChange = { userSchool = it }
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isUpdating) {
                CircularProgressIndicator(color = LightGrey)
            } else {
                PersonalButton(text = "Сохранить изменения") {
                    if (userName.isBlank() || userSName.isBlank() ||  userClass.isBlank()) {
                        errorMessage = "Имя, фамилия и класс обязательны для заполнения"
                        return@PersonalButton
                    }

                    coroutineScope.launch {
                        try {
                            isUpdating = true
                            errorMessage = null

                            val response = userViewModel.updateUserInfo(
                                login = userLogin,
                                name = userName,
                                sName = userSName,
                                uClass = userClass,
                                school = userSchool
                            )

                            if (response.success) {
                                onProfileUpdated()
                            } else {
                                errorMessage = response.message ?: "Не удалось обновить данные"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                        } finally {
                            isUpdating = false
                        }
                    }
                }
            }
        }
    }
}