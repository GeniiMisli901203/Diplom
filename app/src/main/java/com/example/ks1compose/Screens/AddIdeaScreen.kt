package com.example.ks1compose.Screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ks1compose.PersonalUsefulElements.PersonalTextField
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.models.RetrofitInstance
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import com.example.ks1compose.DTOs.NewsDTO

@Composable
fun AddIdeaScreen(
    onIdeaAdded: () -> Unit,
    token: String
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PersonalTextField(2, false, title, "Заголовок", 15) { title = it }
        Spacer(modifier = Modifier.height(10.dp))
        PersonalTextField(5, false, description, "Описание", 15) { description = it }
        Spacer(modifier = Modifier.height(10.dp))
        PersonalTextField(5, false, link, "Ссылка", 15) { link = it }
        Spacer(modifier = Modifier.height(10.dp))

        PersonalButton("Отправить") {
            if (title.isNotBlank() && description.isNotBlank() && link.isNotBlank()) {
                coroutineScope.launch {
                    val news = NewsDTO(
                        userId = token, // Используем токен в качестве userId
                        title = title,
                        description = description,
                        url = link
                    )
                    try {
                        val response = RetrofitInstance.apiService.addNews(news)

                        // Check if the response is successful based on your API structure
                        if (response.isSuccessful) { // Adjust according to your response structure
                            Log.d("AddIdeaScreen", "News added successfully")
                            onIdeaAdded()
                        } else {
                            val apiError = response.errorBody()?.string() ?: "Unknown error"
                            Log.e("AddIdeaScreen", "Failed to add news: $apiError")
                            errorMessage = "Failed to add idea: $apiError" // Update with user-friendly error
                        }
                    } catch (e: Exception) {
                        Log.e("AddIdeaScreen", "Error adding news: ${e.message}", e)
                        errorMessage = "Error adding idea: ${e.message}"
                    }
                }
            } else {
                errorMessage = "All fields are required!"
            }
        }

        // Display error message if exists
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = errorMessage, color = Color.Red)
        }
    }
}
