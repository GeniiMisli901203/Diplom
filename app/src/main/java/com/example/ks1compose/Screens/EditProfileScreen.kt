package com.example.ks1compose.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userViewModel: UserViewModel,
    onProfileUpdated: () -> Unit,
    userLogin: String
) {
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val updateResult by userViewModel.updateResult.collectAsStateWithLifecycle()
    val error by userViewModel.error.collectAsStateWithLifecycle()

    var userName by remember { mutableStateOf("") }
    var userSName by remember { mutableStateOf("") }
    var userClass by remember { mutableStateOf("") }
    var userSchool by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var sNameError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }
    var schoolError by remember { mutableStateOf<String?>(null) }

    // Загружаем текущие данные пользователя
    LaunchedEffect(userInfo) {
        if (userInfo != null) {
            userName = userInfo?.name ?: ""
            userSName = userInfo?.sName ?: ""
            userClass = userInfo?.uClass ?: ""
            userSchool = userInfo?.school ?: ""
        }
    }

    // Обработка результата обновления
    LaunchedEffect(updateResult) {
        when (updateResult) {
            is UserViewModel.UpdateResult.Success -> {
                onProfileUpdated()
                userViewModel.clearUpdateResult()
            }
            else -> {}
        }
    }

    // Загружаем информацию о пользователе при первом входе
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Редактировать профиль",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { onProfileUpdated() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && userInfo == null) {
                PersonalLoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Аватар
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Форма редактирования
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Основная информация",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Имя
                            PersonalTextField(
                                text = userName,
                                label = "Имя",
                                padding = 0,
                                isError = nameError != null,
                                errorMessage = nameError,
                                leadingIcon = Icons.Default.Person,
                                onValueChange = {
                                    userName = it
                                    nameError = null
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Фамилия
                            PersonalTextField(
                                text = userSName,
                                label = "Фамилия",
                                padding = 0,
                                isError = sNameError != null,
                                errorMessage = sNameError,
                                leadingIcon = Icons.Default.Person,
                                onValueChange = {
                                    userSName = it
                                    sNameError = null
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Класс (только для учеников)
                            if (userInfo?.role == "student") {
                                PersonalTextField(
                                    text = userClass,
                                    label = "Класс",
                                    padding = 0,
                                    isError = classError != null,
                                    errorMessage = classError,
                                    leadingIcon = Icons.Default.School,
                                    onValueChange = {
                                        userClass = it
                                        classError = null
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Школа
                            PersonalTextField(
                                text = userSchool,
                                label = "Школа",
                                padding = 0,
                                isError = schoolError != null,
                                errorMessage = schoolError,
                                leadingIcon = Icons.Default.School,
                                onValueChange = {
                                    userSchool = it
                                    schoolError = null
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Кнопка сохранения
                    PersonalButton(
                        text = "Сохранить изменения",
                        onClick = {
                            var isValid = true

                            if (userName.isBlank()) {
                                nameError = "Введите имя"
                                isValid = false
                            }

                            if (userSName.isBlank()) {
                                sNameError = "Введите фамилию"
                                isValid = false
                            }

                            if (userInfo?.role == "student" && userClass.isBlank()) {
                                classError = "Введите класс"
                                isValid = false
                            }

                            if (userSchool.isBlank()) {
                                schoolError = "Введите школу"
                                isValid = false
                            }

                            if (isValid) {
                                userViewModel.updateUserInfo(
                                    name = userName,
                                    sName = userSName,
                                    uClass = userClass,
                                    school = userSchool
                                )
                            }
                        },
                        widthFactor = 1f,
                        isLoading = isLoading && updateResult is UserViewModel.UpdateResult.Loading
                    )

                    // Сообщение об ошибке
                    if (error != null && updateResult is UserViewModel.UpdateResult.Error) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = error!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Сообщение об успехе
                    if (updateResult is UserViewModel.UpdateResult.Success) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Профиль успешно обновлен!",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}