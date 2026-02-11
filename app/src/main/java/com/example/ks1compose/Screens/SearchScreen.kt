package com.example.ks1compose.Screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.repositories.NewsViewModel
import com.example.ks1compose.ui.theme.DarkPink

@Composable
fun SearchScreen(
    viewModel: NewsViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    currentUserId: String
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchHistory by rememberSaveable { mutableStateOf(listOf<String>()) }
    var searchResults by rememberSaveable { mutableStateOf<List<IdeaWithState>>(emptyList()) }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sharedPreferences = context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)

    // Загрузка истории поиска из SharedPreferences
    LaunchedEffect(Unit) {
        val history = sharedPreferences.getString("history", "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
        searchHistory = history
    }

    // Наблюдение за изменениями в данных новостей
    val newsResponse by viewModel.newsResponse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(newsResponse) {
        newsResponse?.let { response ->
            if (response.success) {
                searchResults = response.newsList?.map { newsItem ->
                    IdeaWithState(
                        userId = newsItem.userId,
                        title = newsItem.title,
                        description = newsItem.description,
                        url = newsItem.url ?: ""
                    )
                } ?: emptyList()
                isSearching = false
                showHistory = false
            } else {
                Log.d("Ошибка запроса", "Ошибка при запросе")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isEmpty()) {
                        showHistory = true
                    }
                },
                label = { Text("Поиск новостей") },
                modifier = Modifier
                    .weight(1f)
                    .clickable { showHistory = true },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            keyboardController?.hide()
                            showHistory = true
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                }
            )
            IconButton(onClick = { onNavigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
            }
        }

        if (showHistory && searchHistory.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colors.surface)
                    .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    "История поиска:",
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.padding(8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(searchHistory.distinct()) { historyItem ->  // Убираем дубликаты для отображения
                        Text(
                            text = historyItem,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = historyItem
                                    viewModel.searchNews(historyItem)
                                    showHistory = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (searchQuery.isNotEmpty()) {
                    isSearching = true
                    viewModel.searchNews(searchQuery)

                    // Обновляем историю поиска
                    val updatedHistory = listOf(searchQuery) + searchHistory.filter { it != searchQuery }
                    searchHistory = updatedHistory.take(10)  // Ограничиваем историю 10 элементами

                    // Сохраняем историю в SharedPreferences
                    sharedPreferences.edit()
                        .putString("history", searchHistory.joinToString(","))
                        .apply()

                    showHistory = false
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Поиск")
        }

        if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            if (searchResults.isEmpty() && searchQuery.isNotEmpty() && !isSearching) {
                Text(
                    text = "Ничего не найдено",
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    color = DarkPink
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { item ->
                        IdeaCard(
                            idea = item,
                            currentUserId = currentUserId,
                            onDelete = { /* Пустая функция, так как удаление не требуется */ }
                        )
                    }
                }
            }
        }
    }
}
