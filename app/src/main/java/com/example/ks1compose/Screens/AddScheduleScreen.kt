package com.example.ks1compose.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.DTOs.ScheduleDTO
import com.example.ks1compose.repositories.ScheduleViewModel

@Composable
fun AddScheduleScreen(
    onNavigateBack: () -> Unit
) {
    val daysList = listOf("пн", "вт", "ср", "чт", "пт", "сб")
    val fullDaysMap = mapOf(
        "пн" to "Понедельник",
        "вт" to "Вторник",
        "ср" to "Среда",
        "чт" to "Четверг",
        "пт" to "Пятница",
        "сб" to "Суббота"
    )
    val classList = listOf("5А", "5Б", "6А", "6Б", "7А", "7Б", "8А", "8Б", "9А", "9Б", "10А", "10Б", "11А", "11Б")
    val context = LocalContext.current
    val viewModel: ScheduleViewModel = viewModel()
    val scheduleResponse by viewModel.scheduleResponse.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var selectedDay by rememberSaveable { mutableStateOf("") }
    var selectedClass by rememberSaveable { mutableStateOf("") }
    var lessons by rememberSaveable { mutableStateOf(List(9) { "" }) }
    var office by rememberSaveable { mutableStateOf(List(9) { "" }) }

    LaunchedEffect(scheduleResponse) {
        scheduleResponse?.let { response ->
            if (response.success && response.message == "Расписание успешно добавлено") {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            // Показать Toast с ошибкой
        }
    }

    LaunchedEffect(selectedClass, selectedDay) {
        if (selectedClass.isNotBlank() && selectedDay.isNotBlank()) {
            viewModel.getSchedule(selectedClass, selectedDay)
        }
    }

    val currentScheduleResponse by viewModel.scheduleResponse.collectAsState()

    LaunchedEffect(currentScheduleResponse) {
        currentScheduleResponse?.let { response ->
            if (response.success && response.schedules?.isNotEmpty() == true) {
                val schedule = response.schedules?.first()
                lessons = schedule?.lessons ?: List(9) { "" }
                office = schedule?.office ?: List(9) { "" }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        var classExpanded by remember { mutableStateOf(false) }
        var dayExpanded by remember { mutableStateOf(false) }

        // Выбор класса
        Box {
            TextField(
                value = selectedClass,
                onValueChange = { selectedClass = it },
                label = { Text("Выберите класс") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { classExpanded = true }
                    )
                }
            )
            DropdownMenu(
                expanded = classExpanded,
                onDismissRequest = { classExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                classList.forEach { className ->
                    DropdownMenuItem(
                        text = { Text(className) },
                        onClick = {
                            selectedClass = className
                            classExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Выбор дня недели
        Box {
            TextField(
                value = fullDaysMap[selectedDay] ?: "",
                onValueChange = { selectedDay = it },
                label = { Text("Выберите день недели") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.clickable { dayExpanded = true }
                    )
                }
            )
            DropdownMenu(
                expanded = dayExpanded,
                onDismissRequest = { dayExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                daysList.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(fullDaysMap[day] ?: "") },
                        onClick = {
                            selectedDay = day
                            dayExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список уроков
        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                for (i in 0 until 9) {
                    LessonCard(
                        lessonNumber = i + 1,
                        lesson = lessons.getOrNull(i) ?: "",
                        office = office.getOrNull(i) ?: "",
                        onLessonChange = { newValue ->
                            lessons = lessons.toMutableList().also { it[i] = newValue }
                        },
                        onOfficeChange = { newValue ->
                            office = office.toMutableList().also { it[i] = newValue }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка добавления
        Button(
            onClick = {
                if (selectedClass.isBlank() || selectedDay.isBlank()) {
                    // Показать ошибку
                    return@Button
                }

                val schedule = ScheduleDTO(
                    className = selectedClass,
                    day = selectedDay,
                    lessons = lessons,
                    office = office
                )

                viewModel.addSchedule(schedule)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Добавить расписание")
        }
    }
}

@Composable
fun LessonCard(
    lessonNumber: Int,
    lesson: String,
    office: String,
    onLessonChange: (String) -> Unit,
    onOfficeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = lesson,
                onValueChange = onLessonChange,
                label = { Text("Урок $lessonNumber") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            TextField(
                value = office,
                onValueChange = onOfficeChange,
                label = { Text("Кабинет") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
    }
}
