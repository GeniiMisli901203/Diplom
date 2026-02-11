package com.example.ks1compose.DTOs

import com.google.gson.annotations.SerializedName

data class ScheduleDTO(
    @SerializedName("scheduleId") val scheduleId: String = "",
    @SerializedName("className") val className: String,
    @SerializedName("day") val day: String,
    @SerializedName("lessons") val lessons: List<String>,
    @SerializedName("office") val office: List<String>
)

data class ScheduleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("schedules") val schedules: List<ScheduleDTO>? = null
)