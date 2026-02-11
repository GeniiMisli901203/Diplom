package com.example.ks1compose.models

import com.example.ks1compose.DTOs.NewsDTO
import com.example.ks1compose.DTOs.NewsResponse
import com.example.ks1compose.DTOs.ScheduleDTO
import com.example.ks1compose.DTOs.ScheduleResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/register")
    suspend fun registerUser(@Body request: RegistrationRequest): Response<TokenResponse>

    @POST("/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<TokenResponse>

    @GET("/user/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Response<UserInformationResponse>

    @GET("/user/info")
    suspend fun getUserInfoByToken(
        @Header("Authorization") token: String
    ): Response<UserInformationResponse>

    @POST("/news")
    suspend fun addNews(@Body news: NewsDTO): Response<NewsResponse>

    @GET("/news")
    suspend fun getAllNews(): Response<NewsResponse>

    @POST("/schedule/create")
    suspend fun addSchedule(@Body schedule: ScheduleDTO): Response<ScheduleResponse>

    @GET("/{className}/{day}")
    suspend fun getSchedule(
        @Path("className") className: String,
        @Path("day") day: String
    ): Response<ScheduleResponse>

    @PUT("/user/{login}")
    suspend fun updateUserInfo(
        @Path("login") login: String,
        @Body userInfo: UserInfoRequest
    ): Response<UserInformationResponse>


    @GET("/news/search/{query}")
    suspend fun searchNews(
        @Path("query") query: String
    ): Response<NewsResponse>

    @GET("schedule/{day}")
    suspend fun getSchedulesByDay(
        @Path("day") day: String
    ): Response<ScheduleResponse>

    @GET("/user/{login}")
    suspend fun getUserByLogin(
        @Path("login") login: String
    ): Response<UserInformationResponse>

    @GET("/schedule")
    suspend fun getAllSchedules(): Response<ScheduleResponse>

    @DELETE("/schedule/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("scheduleId") scheduleId: String
    ): Response<ScheduleResponse>

    @DELETE("/news/{newsId}")
    suspend fun deleteNews(
        @Path("newsId") newsId: String
    ): Response<NewsResponse>
}
