package com.example.ks1compose.Screens

import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
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
import com.example.ks1compose.models.TokenManager
import com.example.ks1compose.viewmodels.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIdeaScreen(
    newsViewModel: NewsViewModel,
    onIdeaAdded: () -> Unit,
    token: String
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    val isLoading by newsViewModel.isLoading.collectAsStateWithLifecycle()
    val addNewsResult by newsViewModel.addNewsResult.collectAsStateWithLifecycle()
    val errorMessage by newsViewModel.errorMessage.collectAsStateWithLifecycle()

    // Обработка результата добавления
    LaunchedEffect(addNewsResult) {
        if (addNewsResult != null) {
            onIdeaAdded()
            newsViewModel.clearResults()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Добавить новость",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onIdeaAdded) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
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
                            text = "Новая новость",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Заполните информацию о новости",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Заголовок
                        PersonalTextField(
                            text = title,
                            label = "Заголовок",
                            padding = 0,
                            isError = titleError != null,
                            errorMessage = titleError,
                            leadingIcon = Icons.Default.Title,
                            maxLines = 2,
                            onValueChange = {
                                title = it
                                titleError = null
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Описание
                        PersonalTextField(
                            text = description,
                            label = "Описание",
                            padding = 0,
                            isError = descriptionError != null,
                            errorMessage = descriptionError,
                            leadingIcon = Icons.Default.Description,
                            maxLines = 5,
                            singleLine = false,
                            onValueChange = {
                                description = it
                                descriptionError = null
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ссылка (опционально)
                        PersonalTextField(
                            text = url,
                            label = "Ссылка (необязательно)",
                            padding = 0,
                            leadingIcon = Icons.Default.Link,
                            onValueChange = { url = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка публикации
                PersonalButton(
                    text = "Опубликовать",
                    onClick = {
                        var isValid = true

                        if (title.isBlank()) {
                            titleError = "Введите заголовок"
                            isValid = false
                        }

                        if (description.isBlank()) {
                            descriptionError = "Введите описание"
                            isValid = false
                        }

                        if (isValid) {
                            newsViewModel.addNews(
                                token = token,
                                title = title,
                                description = description,
                                url = url.takeIf { it.isNotBlank() }
                            )
                        }
                    },
                    widthFactor = 1f,
                    isLoading = isLoading
                )

                // Сообщение об ошибке
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Информация о пользователе
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Публикуется от имени: ${TokenManager.userName ?: "Пользователь"}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (isLoading) {
                PersonalLoadingIndicator()
            }
        }
    }
}