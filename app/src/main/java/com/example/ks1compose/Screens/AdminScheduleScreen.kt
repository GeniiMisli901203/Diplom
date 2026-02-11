package com.example.ks1compose.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.repositories.ScheduleViewModel
import com.example.ks1compose.ui.theme.DarkGrey
import com.example.ks1compose.ui.theme.DarkPink

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AdminScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val daysList = listOf("пн", "вт", "ср", "чт", "пт", "сб")
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    var schedule by rememberSaveable { mutableStateOf(mapOf<String, Map<String, Map<String, List<String>>>>()) }
    var isLoading by rememberSaveable { mutableStateOf(true) }

    // Получение данных расписания для всех классов по дню
    LaunchedEffect(pagerState.currentPage) {
        val currentDay = daysList[pagerState.currentPage]
        viewModel.getSchedulesByDay(currentDay)
    }

    // Наблюдение за изменениями в данных расписания
    val scheduleResponse by viewModel.scheduleResponse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(scheduleResponse) {
        scheduleResponse?.let { response ->
            if (response.success) {
                response.schedules?.let { schedules ->
                    schedule = schedules.groupBy { it.day }.mapValues { (_, schedulesForDay) ->
                        schedulesForDay.associate { scheduleDTO ->
                            scheduleDTO.className to mapOf(
                                "Lessons" to scheduleDTO.lessons,
                                "Office" to scheduleDTO.office
                            )
                        }
                    }
                    isLoading = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = DarkGrey)
        } else {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                    )
                },
                contentColor = DarkPink,
                backgroundColor = MaterialTheme.colors.background,
                modifier = Modifier.clip(RoundedCornerShape(25.dp))
            ) {
                daysList.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = {
                            Text(
                                text = day,
                                color = if (pagerState.currentPage == index) DarkPink else MaterialTheme.colors.onSurface
                            )
                        }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                count = daysList.size,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentDay = daysList[page]
                val daySchedule = schedule[currentDay] ?: mapOf()

                Log.d("AdminScheduleScreen", "Day schedule for $currentDay: $daySchedule")

                if (daySchedule.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет расписания",
                            color = MaterialTheme.colors.onSurface,
                            style = MaterialTheme.typography.h5
                        )
                    }
                } else {
                    val sortedClassSchedule = daySchedule.toList().sortedBy { (className, _) ->
                        className.filter { it.isDigit() }.toIntOrNull() ?: 0
                    }.toMap()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(sortedClassSchedule.size) { index ->
                            val className = sortedClassSchedule.keys.elementAt(index)
                            val classSchedule = sortedClassSchedule[className] ?: mapOf(
                                "Lessons" to List(9) { "" },
                                "Office" to List(9) { "" }
                            )
                            Log.d("AdminScheduleScreen", "Class schedule for $className: $classSchedule")
                            ClassScheduleCard(
                                className = className,
                                lessons = classSchedule["Lessons"] ?: listOf(),
                                offices = classSchedule["Office"] ?: listOf()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClassScheduleCard(className: String, lessons: List<String>, offices: List<String>) {
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
                text = "Класс: $className",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(5.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colors.onSurface))
            Spacer(modifier = Modifier.height(5.dp))
            lessons.forEachIndexed { index, lesson ->
                val office = offices.getOrNull(index) ?: "Нет кабинета"
                Text(
                    text = "Урок ${index + 1}: $lesson",
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "Кабинет: $office",
                    color = MaterialTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

