import com.example.ks1compose.models.RetrofitInstance

import com.example.ks1compose.models.UserInformationResponse

class UserRepository {
    private val apiService = RetrofitInstance.apiService

    suspend fun getUserInfo(login: String): UserInformationResponse {
        val response = apiService.getUserByLogin(login)
        if (!response.isSuccessful) {
            throw Exception("Ошибка: ${response.code()}")
        }
        return response.body() ?: throw Exception("Пустой ответ")
    }
}
