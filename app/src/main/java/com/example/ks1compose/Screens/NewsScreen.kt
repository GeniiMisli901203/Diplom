package com.example.ks1compose.Screens

import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.example.ks1compose.models.RetrofitInstance
import com.example.ks1compose.repositories.NewsViewModel
import com.example.ks1compose.ui.theme.VeryLightGrey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

data class IdeaWithState(val userId: String, val title: String, val description: String, val url: String)
data class LinkPreview(val title: String?, val description: String?, val imageUrl: String?)

@Composable
fun IdeaCard(
    idea: IdeaWithState,
    currentUserId: String, // Передаем идентификатор текущего пользователя
    onDelete: (String) -> Unit,
) {
    var isVisible by remember { mutableStateOf(true) }
    var linkPreview by remember { mutableStateOf<LinkPreview?>(null) }
    val context = LocalContext.current

    val isDarkTheme = isSystemInDarkTheme()
    val cardColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
    } else {
        Color.LightGray
    }

    LaunchedEffect(idea.url) {
        linkPreview = fetchLinkPreview(idea.url)
    }

    if (isVisible) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clip(RoundedCornerShape(30.dp)),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .border(3.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(30.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = idea.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (MaterialTheme.colorScheme.secondary == VeryLightGrey) Color.Black else Color.White
                    )

                    // Проверяем, является ли текущий пользователь администратором
                    if (currentUserId == "m.znakin@mail.ru") {
                        IconButton(
                            onClick = {
                                isVisible = false
                                onDelete(idea.userId)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = Color.Red
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface))
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = idea.description,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (linkPreview != null) {
                    Text(
                        text = linkPreview?.title ?: "No Title",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = linkPreview?.description ?: "No Description",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Text(
                    text = idea.url,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        openUrl(idea.url, context)
                    }
                )
            }
        }
    }
}



private fun openUrl(url: String?, context: Context) {
    if (!url.isNullOrEmpty()) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
}

suspend fun fetchLinkPreview(link: String): LinkPreview? {
    return withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(link).get()
            val title = doc.title()
            val description = doc.select("meta[name=description]").attr("content")
            val imageUrl = doc.select("meta[property=og:image]").attr("content").takeIf { it.isNotEmpty() }

            LinkPreview(title = title, description = description, imageUrl = imageUrl)
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun IdeaScreen(
    onNavigateToSearch: () -> Unit,
    viewModel: NewsViewModel = viewModel(),
    currentUserId: String // Передаем идентификатор текущего пользователя
) {
    val coroutineScope = rememberCoroutineScope()
    var ideas by remember { mutableStateOf<List<IdeaWithState>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun fetchIdeas() {
        isLoading = true
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.apiService.getAllNews()

                if (response.isSuccessful) {
                    val body = response.body()
                    ideas = body?.newsList?.map { newsItem ->
                        IdeaWithState(newsItem.userId, newsItem.title, newsItem.description, newsItem.url)
                    }?.reversed() ?: emptyList()
                } else {
                    error = response.message()
                }
            } catch (e: Exception) {
                error = "Ошибка подключения: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteIdea(newsId: String) {
        coroutineScope.launch {
            try {
                viewModel.deleteNews(newsId)
                fetchIdeas()
            } catch (e: Exception) {
                error = "Ошибка удаления: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchIdeas()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Gray
            )
        } else if (error != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Ошибка: $error", modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { fetchIdeas() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Обновить")
                }
            }
        } else if (ideas.isEmpty()) {
            Text(
                text = "Нет результатов поиска",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(30.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onNavigateToSearch() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Поиск")
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ideas) { item ->
                        IdeaCard(
                            idea = item,
                            currentUserId = currentUserId, // Передаем идентификатор текущего пользователя
                            onDelete = { deleteIdea(it) }
                        )
                    }
                }
            }
        }
    }
}