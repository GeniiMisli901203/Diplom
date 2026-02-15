// com.example.ks1compose.Screens.AdminScreen.kt
package com.example.ks1compose.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ks1compose.PersonalUsefulElements.PersonalLoadingIndicator
import com.example.ks1compose.models.StudentUIModel
import com.example.ks1compose.models.UserDTO
import com.example.ks1compose.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onUserClick: (String, String) -> Unit = { _, _ -> }
) {
    val teachers by userViewModel.teachers.collectAsStateWithLifecycle()
    val students by userViewModel.students.collectAsStateWithLifecycle() // Это List<StudentUIModel>
    val isLoading by userViewModel.isLoading.collectAsStateWithLifecycle()
    val error by userViewModel.error.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Ученики", "Учителя")

    // Загружаем данные при первом входе
    LaunchedEffect(Unit) {
        userViewModel.loadAllTeachers()
        userViewModel.loadAllStudents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Администрирование", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Табы для переключения между учениками и учителями
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTab == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Контент в зависимости от выбранного таба
            when (selectedTab) {
                0 -> StudentsContent(
                    students = students,
                    isLoading = isLoading,
                    error = error,
                    onRefresh = { userViewModel.loadAllStudents() },
                    onStudentClick = { studentId, className ->
                        onUserClick(studentId, className)
                    }
                )
                1 -> TeachersContent(
                    teachers = teachers,
                    isLoading = isLoading,
                    error = error,
                    onRefresh = { userViewModel.loadAllTeachers() }
                )
            }
        }
    }
}

@Composable
fun StudentsContent(
    students: List<StudentUIModel>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onStudentClick: (String, String) -> Unit
) {
    if (isLoading && students.isEmpty()) {
        PersonalLoadingIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Статистика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AdminStatItem(
                    value = students.size.toString(),
                    label = "Всего учеников"
                )

                val classCount = students.groupBy { it.className ?: "Без класса" }.size
                AdminStatItem(
                    value = classCount.toString(),
                    label = "Классов"
                )
            }
        }

        if (error != null) {
            AdminErrorMessage(
                message = error,
                onRetry = onRefresh
            )
        } else if (students.isEmpty()) {
            AdminEmptyUsersMessage(type = "учеников")
        } else {
            // Группировка учеников по классам
            val studentsByClass = students
                .filter { it.className != null && it.className!!.isNotBlank() }
                .groupBy { it.className!! }

            // Получаем отсортированный список классов
            val sortedClasses = sortClasses(studentsByClass.keys)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Сначала идут классы с сортировкой
                sortedClasses.forEach { className ->
                    val classStudents = studentsByClass[className] ?: emptyList()
                    item {
                        StudentClassSection(
                            className = className,
                            students = classStudents,
                            onStudentClick = onStudentClick
                        )
                    }
                }

                // Если есть ученики без класса, показываем их в конце
                val studentsWithoutClass = students.filter { it.className.isNullOrBlank() }
                if (studentsWithoutClass.isNotEmpty()) {
                    item {
                        StudentClassSection(
                            className = "Без класса",
                            students = studentsWithoutClass,
                            onStudentClick = onStudentClick
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun StudentClassSection(
    className: String,
    students: List<StudentUIModel>, // Используем StudentUIModel
    onStudentClick: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Заголовок класса
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$className класс",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${students.size} учеников",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        // Список учеников класса
        students.forEach { student ->
            StudentCard(
                student = student,
                onClick = { onStudentClick(student.id, className) }
            )
        }
    }
}

@Composable
fun StudentCard(
    student: StudentUIModel, // Используем StudentUIModel
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар с инициалами
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = student.name.split(" ").take(2).joinToString("") { it.take(1) },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация об ученике
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = student.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (student.averageGrade != null) {
                    Text(
                        text = "Ср. балл: ${String.format("%.2f", student.averageGrade)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Кнопка действий
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Перейти",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TeachersContent(
    teachers: List<UserDTO>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    if (isLoading && teachers.isEmpty()) {
        PersonalLoadingIndicator()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Статистика
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AdminStatItem(  // Переименовано
                    value = teachers.size.toString(),
                    label = "Всего учителей"
                )
            }
        }

        if (error != null) {
            AdminErrorMessage(  // Переименовано
                message = error,
                onRetry = onRefresh
            )
        } else if (teachers.isEmpty()) {
            AdminEmptyUsersMessage(type = "учителей")  // Переименовано
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(teachers) { teacher ->
                    TeacherCard(teacher = teacher)
                }
            }
        }
    }
}

@Composable
fun TeacherCard(
    teacher: UserDTO
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Информация об учителе
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${teacher.name ?: ""} ${teacher.sName ?: ""}".trim(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = teacher.email ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                // Проверяем на null и пустую строку безопасно
                if (teacher.uClass != null && teacher.uClass.isNotBlank()) {
                    Text(
                        text = "Классный руководитель: ${teacher.uClass}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun AdminErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.Red
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Повторить")
        }
    }
}

@Composable
fun AdminEmptyUsersMessage(
    type: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет $type",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}


fun sortClasses(classes: Set<String>): List<String> {
    return classes.sortedWith { a, b ->
        val aNum = a.filter { it.isDigit() }.toIntOrNull() ?: 0
        val bNum = b.filter { it.isDigit() }.toIntOrNull() ?: 0

        if (aNum != bNum) {
            aNum.compareTo(bNum)  // Сортируем по числу
        } else {
            // Если числа равны, сортируем по букве
            val aLetter = a.filter { it.isLetter() }
            val bLetter = b.filter { it.isLetter() }
            aLetter.compareTo(bLetter)
        }
    }
}