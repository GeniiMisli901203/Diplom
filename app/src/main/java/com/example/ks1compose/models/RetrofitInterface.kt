package com.example.ks1compose.models

import com.example.ks1compose.DTOs.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ================ Аутентификация ================
    @POST("/register")
    suspend fun registerUser(@Body request: RegistrationRequest): Response<TokenResponse>

    @POST("/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<TokenResponse>

    // ================ Пользователи ================
    @GET("/user/info")
    suspend fun getUserInfoByToken(
        @Header("Authorization") token: String
    ): Response<UserInformationResponse>

    @GET("/user/login/{login}")
    suspend fun getUserByLogin(
        @Path("login") login: String
    ): Response<UserInformationResponse>

    @GET("/user/id/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String
    ): Response<UserInformationResponse>

    @PUT("/user/update")
    suspend fun updateUserInfo(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<UserInformationResponse>

    @DELETE("/user/delete")
    suspend fun deleteUser(
        @Header("Authorization") token: String
    ): Response<UserInformationResponse>

    @GET("/user/teachers")
    suspend fun getAllTeachers(): Response<UsersListResponse>

    @GET("/user/class/{className}/students")
    suspend fun getStudentsByClass(
        @Path("className") className: String
    ): Response<UsersListResponse>

    // ================ Оценки ================
    @POST("/grades/add")
    suspend fun addGrade(
        @Header("Authorization") token: String,
        @Body grade: GradeDTO
    ): Response<GradeResponse>

    @GET("/grades/my")
    suspend fun getMyGrades(
        @Header("Authorization") token: String,
        @Query("subject") subject: String? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<GradeResponse>

    @GET("/grades/teacher")
    suspend fun getTeacherGrades(
        @Header("Authorization") token: String,
        @Query("class") className: String? = null,
        @Query("subject") subject: String? = null,
        @Query("studentId") studentId: String? = null
    ): Response<GradeResponse>

    @GET("/grades/class/{className}")
    suspend fun getClassGrades(
        @Header("Authorization") token: String,
        @Path("className") className: String,
        @Query("subject") subject: String? = null
    ): Response<GradeResponse>

    @PUT("/grades/{gradeId}")
    suspend fun updateGrade(
        @Header("Authorization") token: String,
        @Path("gradeId") gradeId: String,
        @Body request: UpdateGradeRequest
    ): Response<GradeResponse>

    @DELETE("/grades/{gradeId}")
    suspend fun deleteGrade(
        @Header("Authorization") token: String,
        @Path("gradeId") gradeId: String
    ): Response<GradeResponse>

    // ================ Расписание (новое) ================
    @GET("/lessons/{className}/{dayOfWeek}")
    suspend fun getLessonsByClassAndDay(
        @Path("className") className: String,
        @Path("dayOfWeek") dayOfWeek: String,
        @Query("weekNumber") weekNumber: Int? = null
    ): Response<LessonsResponse>

    @GET("/lessons/weekly/{className}")
    suspend fun getWeeklySchedule(
        @Path("className") className: String,
        @Query("weekNumber") weekNumber: Int? = null
    ): Response<WeeklyScheduleResponse>

    @POST("/lessons/manage")
    suspend fun createLesson(
        @Header("Authorization") token: String,
        @Body lesson: LessonDTO
    ): Response<LessonsResponse>

    @PUT("/lessons/{lessonId}")
    suspend fun updateLesson(
        @Header("Authorization") token: String,
        @Path("lessonId") lessonId: String,
        @Body updates: Map<String, Any?>
    ): Response<LessonsResponse>

    @DELETE("/lessons/{lessonId}")
    suspend fun deleteLesson(
        @Header("Authorization") token: String,
        @Path("lessonId") lessonId: String
    ): Response<LessonsResponse>

    // ================ Расписание (старое) ================
    @GET("/schedule")
    suspend fun getAllSchedules(): Response<ScheduleResponse>

    @GET("/schedule/{day}")
    suspend fun getSchedulesByDay(
        @Path("day") day: String
    ): Response<ScheduleResponse>

    @GET("/{className}/{day}")
    suspend fun getSchedule(
        @Path("className") className: String,
        @Path("day") day: String
    ): Response<ScheduleResponse>

    @POST("/schedule/create")
    suspend fun addSchedule(
        @Header("Authorization") token: String,
        @Body schedule: ScheduleDTO
    ): Response<ScheduleResponse>

    @DELETE("/schedule/{scheduleId}")
    suspend fun deleteSchedule(
        @Header("Authorization") token: String,
        @Path("scheduleId") scheduleId: String
    ): Response<ScheduleResponse>

    // ================ Учителя и предметы ================
    @POST("/teacher/subjects")
    suspend fun addTeacherSubject(
        @Header("Authorization") token: String,
        @Body request: AddSubjectRequest
    ): Response<TeacherSubjectsResponse>

    @GET("/teacher/{teacherId}/subjects")
    suspend fun getTeacherSubjects(
        @Path("teacherId") teacherId: String
    ): Response<TeacherSubjectsResponse>

    // ================ НОВОСТИ ================
    @GET("/news")
    suspend fun getAllNews(): Response<NewsResponse>

    @POST("/news/create")
    suspend fun addNews(
        @Header("Authorization") token: String,
        @Body news: NewsDTO
    ): Response<NewsResponse>

    @GET("/news/search/{query}")
    suspend fun searchNews(
        @Path("query") query: String
    ): Response<NewsResponse>

    @DELETE("/news/{newsId}")
    suspend fun deleteNews(
        @Header("Authorization") token: String,
        @Path("newsId") newsId: String
    ): Response<NewsResponse>

    // ================ Вспомогательные эндпоинты ================
    @GET("/user/role-check")
    suspend fun checkRole(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("/grades/today/{studentId}")
    suspend fun getTodayGrades(
        @Path("studentId") studentId: String,
        @Header("Authorization") token: String
    ): Response<GradeResponse>

    // Получить оценки всего класса с группировкой по ученикам
    @GET("/grades/class/{className}/students")
    suspend fun getClassStudentsGrades(
        @Path("className") className: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    // Получить средний балл ученика по предметам
    @GET("/grades/average/{studentId}/subjects")
    suspend fun getStudentAveragesBySubject(
        @Path("studentId") studentId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    @GET("/students/all")
    suspend fun getAllStudents(): Response<StudentsListResponse>


}