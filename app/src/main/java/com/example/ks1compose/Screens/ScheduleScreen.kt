package com.example.ks1compose.Screens

import UserRepository
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Card
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.CircularProgressIndicator
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Tab
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TabRow
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TabRowDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.repositories.ScheduleViewModel
import com.example.ks1compose.ui.theme.DarkGrey
import com.example.ks1compose.ui.theme.DarkPink
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel(), userLogin: String) {
    val daysList = listOf("пн", "вт", "ср", "чт", "пт", "сб")
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    var userClass by rememberSaveable { mutableStateOf("") }
    var schedule by rememberSaveable { mutableStateOf(mapOf<String, Map<String, List<String>>>()) }
    var isLoading by rememberSaveable { mutableStateOf(true) }

    val userRepository = remember { UserRepository() }

    // Получение класса пользователя
    LaunchedEffect(userLogin) {
        coroutineScope.launch {
            try {
                val userInfo = userRepository.getUserInfo(userLogin)
                if (userInfo.success && userInfo.user != null) {
                    userClass = userInfo.user.uClass
                }
            } catch (e: Exception) {
                Log.e("ScheduleScreen", "Error fetching user info", e)
            }
        }
    }

    // Получение данных расписания
    LaunchedEffect(userClass, pagerState.currentPage) {
        if (userClass.isNotEmpty()) {
            viewModel.getSchedule(userClass, daysList[pagerState.currentPage])
        }
    }

    // Наблюдение за изменениями в данных расписания
    val scheduleResponse by viewModel.scheduleResponse.collectAsState()

    LaunchedEffect(scheduleResponse) {
        scheduleResponse?.let { response ->
            if (response.success) {
                response.schedules?.let { schedules ->
                    schedule = schedules.associate { scheduleDTO ->
                        scheduleDTO.day to mapOf(
                            "Lessons" to scheduleDTO.lessons,
                            "Office" to scheduleDTO.office
                        )
                    }
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                )
            },
            contentColor = DarkPink,
            backgroundColor = MaterialTheme.colors.surface,
            modifier = Modifier.clip(RoundedCornerShape(25.dp))
        ) {
            daysList.forEachIndexed { index, day ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(day, color = if (pagerState.currentPage == index) DarkPink else MaterialTheme.colors.onSurface) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            count = daysList.size
        ) { page ->
            val currentDay = daysList[page]
            val daySchedule = schedule[currentDay] ?: mapOf(
                "Lessons" to List(9) { "" },
                "Office" to List(9) { "" }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Gray)
                }
            } else if (daySchedule["Lessons"]?.isEmpty() == true) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет расписания",
                        color = MaterialTheme.colors.onSurface,
                        style = MaterialTheme.typography.h6
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(9) { index ->
                        val lesson = daySchedule["Lessons"]?.getOrNull(index) ?: "Нет урока"
                        val office = daySchedule["Office"]?.getOrNull(index) ?: "Нет кабинета"
                        LessonCard(
                            subject = lesson,
                            office = office,
                            startTime = getLessonStartTime(userClass, index + 1),
                            endTime = getLessonEndTime(userClass, index + 1),
                            lessonNumber = index + 1
                        )
                        if (index < 8) {
                            BreakCard(
                                startTime = getBreakStartTime(userClass, index + 1),
                                endTime = getBreakEndTime(userClass, index + 1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCard(subject: String, office: String, startTime: String, endTime: String, lessonNumber: Int) {
    Log.d("LessonCard", "Subject: $subject, Office: $office, StartTime: $startTime, EndTime: $endTime, LessonNumber: $lessonNumber")


    Card(
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(25.dp))
    ) {
        Column(modifier = Modifier
            .border(3.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), RoundedCornerShape(25.dp))
            .padding(16.dp)) {
            Text(
                text = "Урок $lessonNumber: $subject",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colors.onSurface,
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    shadow = Shadow(
                        color = Color.Gray,
                        offset = Offset(2.0f, 2.0f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface))
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Кабинет: $office",
                color = MaterialTheme.colors.onSurface,
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    shadow = Shadow(
                        color = Color.Gray,
                        offset = Offset(2.0f, 2.0f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Время: $startTime - $endTime",
                color = MaterialTheme.colors.onSurface,
                style = TextStyle(
                    fontFamily = FontFamily.Cursive,
                    shadow = Shadow(
                        color = Color.Gray,
                        offset = Offset(2.0f, 2.0f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

@Composable
fun BreakCard(startTime: String, endTime: String) {
    Card(
        backgroundColor = MaterialTheme.colors.secondaryVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clip(RoundedCornerShape(25.dp))
    ) {
        Column(modifier = Modifier
            .border(3.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), RoundedCornerShape(25.dp))
            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Время: $startTime - $endTime", color = MaterialTheme.colors.onPrimary)
        }
    }
}

fun getLessonStartTime(userClass: String, lessonNumber: Int): String {
    return when (userClass) {
        "6А", "7Б" -> when (lessonNumber) {
            1 -> "12:00"
            2 -> "12:50"
            3 -> "13:50"
            4 -> "14:50"
            5 -> "15:50"
            6 -> "16:50"
            else -> ""
        }
        "6Б", "7А" -> when (lessonNumber) {
            1 -> "13:40"
            2 -> "14:30"
            3 -> "15:30"
            4 -> "16:30"
            5 -> "17:30"
            6 -> "18:30"
            else -> ""
        }
        else -> when (lessonNumber) {
            1 -> "09:00"
            2 -> "09:50"
            3 -> "10:50"
            4 -> "11:50"
            5 -> "12:45"
            6 -> "13:30"
            else -> ""
        }
    }
}

fun getLessonEndTime(userClass: String, lessonNumber: Int): String {
    return when (userClass) {
        "6А", "7Б" -> when (lessonNumber) {
            1 -> "12:45"
            2 -> "13:40"
            3 -> "14:40"
            4 -> "15:40"
            5 -> "16:40"
            6 -> "17:40"
            else -> ""
        }
        "6Б", "7А" -> when (lessonNumber) {
            1 -> "14:25"
            2 -> "15:20"
            3 -> "16:20"
            4 -> "17:20"
            5 -> "18:20"
            6 -> "19:20"
            else -> ""
        }
        else -> when (lessonNumber) {
            1 -> "09:45"
            2 -> "10:45"
            3 -> "11:45"
            4 -> "12:40"
            5 -> "13:25"
            6 -> "14:15"
            else -> ""
        }
    }
}

fun getBreakStartTime(userClass: String, lessonNumber: Int): String {
    return when (userClass) {
        "6А", "7Б" -> when (lessonNumber) {
            1 -> "12:45"
            2 -> "13:40"
            3 -> "14:40"
            4 -> "15:40"
            5 -> "16:40"
            else -> ""
        }
        "6Б", "7А" -> when (lessonNumber) {
            1 -> "14:25"
            2 -> "15:20"
            3 -> "16:20"
            4 -> "17:20"
            5 -> "18:20"
            else -> ""
        }
        else -> when (lessonNumber) {
            1 -> "09:45"
            2 -> "10:45"
            3 -> "11:45"
            4 -> "12:40"
            5 -> "13:25"
            else -> ""
        }
    }
}

fun getBreakEndTime(userClass: String, lessonNumber: Int): String {
    return when (userClass) {
        "6А", "7Б" -> when (lessonNumber) {
            1 -> "12:50"
            2 -> "13:45"
            3 -> "14:45"
            4 -> "15:45"
            5 -> "16:45"
            else -> ""
        }
        "6Б", "7А" -> when (lessonNumber) {
            1 -> "14:30"
            2 -> "15:25"
            3 -> "16:25"
            4 -> "17:25"
            5 -> "18:25"
            else -> ""
        }
        else -> when (lessonNumber) {
            1 -> "09:50"
            2 -> "10:50"
            3 -> "11:50"
            4 -> "12:45"
            5 -> "13:30"
            else -> ""
        }
    }
}