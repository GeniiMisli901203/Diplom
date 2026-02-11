package com.example.ks1compose.models

import com.example.ks1compose.DTOs.GradeDTO
import com.example.ks1compose.DTOs.LessonDTO

// Модель для отображения оценки в UI
data class GradeUIModel(
    val id: String,
    val subjectName: String,
    val gradeValue: Int,
    val gradeType: String,
    val teacherName: String,
    val comment: String?,
    val date: String,
    val color: androidx.compose.ui.graphics.Color
)

// Модель для отображения урока в UI
data class LessonUIModel(
    val id: String,
    val lessonNumber: Int,
    val subjectName: String,
    val teacherName: String?,
    val room: String?,
    val startTime: String?,
    val endTime: String?,
    val isCurrentLesson: Boolean = false
)

// Модель для отображения студента в UI
data class StudentUIModel(
    val id: String,
    val name: String,
    val className: String,
    val averageGrade: Double? = null
)

// Модель для Dashboard
data class DashboardData(
    val userName: String,
    val userRole: String,
    val className: String? = null,
    val todayLessons: List<LessonUIModel> = emptyList(),
    val recentGrades: List<GradeUIModel> = emptyList(),
    val averageGrade: Double? = null,
    val newsCount: Int = 0
)

// Конвертеры DTO -> UI Model
object ModelConverter {

    fun convertGradeToUIModel(grade: GradeDTO): GradeUIModel {
        val color = when (grade.gradeValue) {
            5 -> androidx.compose.ui.graphics.Color.Green
            4 -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Light Green
            3 -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Amber
            2 -> androidx.compose.ui.graphics.Color.Red
            else -> androidx.compose.ui.graphics.Color.Gray
        }

        return GradeUIModel(
            id = grade.gradeId ?: "",
            subjectName = grade.subjectName,
            gradeValue = grade.gradeValue,
            gradeType = grade.gradeType,
            teacherName = grade.teacherName ?: "Неизвестно",
            comment = grade.comment,
            date = grade.lessonDate,
            color = color
        )
    }

    fun convertLessonToUIModel(lesson: LessonDTO): LessonUIModel {
        return LessonUIModel(
            id = lesson.lessonId ?: "",
            lessonNumber = lesson.lessonNumber,
            subjectName = lesson.subjectName,
            teacherName = lesson.teacherName,
            room = lesson.room,
            startTime = lesson.startTime,
            endTime = lesson.endTime
        )
    }

    fun convertUserToStudentModel(user: UserDTO): StudentUIModel {
        return StudentUIModel(
            id = user.userId,
            name = "${user.name} ${user.sName}",
            className = user.uClass
        )
    }
}