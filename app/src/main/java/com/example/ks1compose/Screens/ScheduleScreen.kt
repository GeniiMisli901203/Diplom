package com.example.ks1compose.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ks1compose.PersonalUsefulElements.PersonalButton
import com.example.ks1compose.PersonalUsefulElements.PersonalCard
import com.example.ks1compose.PersonalUsefulElements.PersonalDropdown
import com.example.ks1compose.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.models.LessonUIModel
import com.example.ks1compose.viewmodels.LessonViewModel
import com.example.ks1compose.viewmodels.UserViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    lessonViewModel: LessonViewModel,
    userViewModel: UserViewModel,
    userLogin: String
) {
    // Используем collectAsState() с правильным импортом
    val userInfo by userViewModel.userInfo.collectAsStateWithLifecycle()
    val todayLessons by lessonViewModel.todayLessons.collectAsStateWithLifecycle()
    val isLoading by lessonViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by lessonViewModel.errorMessage.collectAsStateWithLifecycle()

    var selectedDay by remember { mutableStateOf(getCurrentDayOfWeek()) }
    var selectedWeek by remember { mutableStateOf<Int?>(null) }
    var showWeekSelector by remember { mutableStateOf(false) }
    var showDaySelector by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        "monday" to "Понедельник",
        "tuesday" to "Вторник",
        "wednesday" to "Среда",
        "thursday" to "Четверг",
        "friday" to "Пятница",
        "saturday" to "Суббота",
        "sunday" to "Воскресенье"
    )

    val weekOptions = listOf(
        null to "Текущая неделя",
        1 to "1-я неделя",
        2 to "2-я неделя"
    )

    // Загружаем информацию о пользователе
    LaunchedEffect(Unit) {
        userViewModel.loadUserInfo()
    }

    // Загружаем расписание для ученика
    LaunchedEffect(userInfo?.uClass, selectedDay, selectedWeek) {
        if (userInfo?.role == "student" && !userInfo?.uClass.isNullOrBlank()) {
            lessonViewModel.loadLessonsByDay(userInfo!!.uClass, selectedDay, selectedWeek)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Расписание",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (userInfo?.role == "student" && !userInfo?.uClass.isNullOrBlank()) {
                            Text(
                                text = "${userInfo?.uClass} класс",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Кнопка выбора дня
                    IconButton(onClick = { showDaySelector = true }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Выбрать день"
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
            when (userInfo?.role) {
                "student" -> {
                    if (userInfo?.uClass.isNullOrBlank()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Класс не указан",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        StudentScheduleContent(
                            userClass = userInfo!!.uClass,
                            selectedDay = selectedDay,
                            onDaySelected = { selectedDay = it },
                            daysOfWeek = daysOfWeek,
                            lessons = todayLessons,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            onRefresh = {
                                if (userInfo?.uClass != null) {
                                    lessonViewModel.loadLessonsByDay(userInfo!!.uClass, selectedDay, selectedWeek)
                                }
                            }
                        )
                    }
                }
                "teacher" -> {
                    TeacherScheduleContent(
                        teacherId = userInfo?.userId ?: "",
                        lessonViewModel = lessonViewModel,
                        userViewModel = userViewModel
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Расписание недоступно",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Индикатор загрузки
            if (isLoading && todayLessons.isEmpty()) {
                PersonalLoadingIndicator()
            }
        }
    }

    // Диалог выбора дня
    if (showDaySelector) {
        AlertDialog(
            onDismissRequest = { showDaySelector = false },
            title = { Text("Выберите день") },
            text = {
                Column {
                    daysOfWeek.forEach { (day, dayName) ->
                        RadioButtonWithLabel(
                            selected = selectedDay == day,
                            onClick = {
                                selectedDay = day
                                showDaySelector = false
                            },
                            label = dayName
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Неделя",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    weekOptions.forEach { (week, label) ->
                        RadioButtonWithLabel(
                            selected = selectedWeek == week,
                            onClick = {
                                selectedWeek = week
                            },
                            label = label
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDaySelector = false }) {
                    Text("Готово")
                }
            }
        )
    }
}

@Composable
fun StudentScheduleContent(
    userClass: String,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    daysOfWeek: List<Pair<String, String>>,
    lessons: List<LessonUIModel>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Текущий день
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = daysOfWeek.find { it.first == selectedDay }?.second ?: selectedDay,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = userClass,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Обновить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (errorMessage != null && lessons.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ошибка загрузки",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PersonalButton(
                    text = "Повторить",
                    onClick = onRefresh,
                    widthFactor = 0.5f
                )
            }
        } else if (lessons.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "На этот день нет уроков",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lessons.sortedBy { it.lessonNumber }) { lesson ->
                    LessonItemCard(lesson = lesson)
                }
            }
        }
    }
}

@Composable
fun LessonItemCard(
    lesson: LessonUIModel
) {
    PersonalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (lesson.isCurrentLesson)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        borderColor = if (lesson.isCurrentLesson)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер урока
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (lesson.isCurrentLesson)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lesson.lessonNumber.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (lesson.isCurrentLesson)
                        Color.White
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация об уроке
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = lesson.subjectName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Учитель
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lesson.teacherName ?: "Не указан",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Кабинет
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Room,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lesson.room ?: "Не указан",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Время
                if (lesson.startTime != null && lesson.endTime != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${lesson.startTime} - ${lesson.endTime}",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Индикатор текущего урока
            if (lesson.isCurrentLesson) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Green)
                )
            }
        }
    }
}

@Composable
fun TeacherScheduleContent(
    teacherId: String,
    lessonViewModel: LessonViewModel,
    userViewModel: UserViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Расписание учителя",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Функция в разработке",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Скоро появится",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Возможность просмотра расписания для учителей будет добавлена в ближайшее время",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun RadioButtonWithLabel(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = Color.Gray
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getCurrentDayOfWeek(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
    return LocalDate.now().format(formatter).lowercase(Locale.ENGLISH)
}