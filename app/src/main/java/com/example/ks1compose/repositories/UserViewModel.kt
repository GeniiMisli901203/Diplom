package com.example.ks1compose.repositories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.ks1compose.models.RetrofitInstance.apiService
import com.example.ks1compose.models.UserInfoRequest
import com.example.ks1compose.models.UserInformationResponse

class UserViewModel(application: Application) : AndroidViewModel(application) {

    suspend fun updateUserInfo(
        login: String,
        name: String,
        sName: String,
        uClass: String,
        school: String
    ): UserInformationResponse {
        val userInfoRequest = UserInfoRequest(
            name = name,
            sName = sName,
            uClass = uClass,
            school = school
        )

        val response = apiService.updateUserInfo(
            login = login,  // Используйте параметр login, а не userLogin
            userInfo = userInfoRequest
        )

        if (!response.isSuccessful) {
            throw Exception("Ошибка: ${response.code()}")
        }
        return response.body() ?: throw Exception("Пустой ответ")
    }
}
