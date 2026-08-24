package com.example.appmobile.domain.usecase

import com.example.appmobile.data.remote.api.RegisterUserApiService
import com.example.appmobile.data.remote.dto.RegisterUserRequestDto
import com.example.appmobile.data.remote.dto.RegisterUserResponseDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient

class RegisterUserUseCase {
    private val apiService: RegisterUserApiService

    init {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(com.example.appmobile.data.remote.FirebaseAuthInterceptor())
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.appmobile.BuildConfig.USER_REGISTRATION_API_BASE_URL)
            .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(RegisterUserApiService::class.java)
    }
    
    suspend operator fun invoke(
        uidUser: String,
        weightKg: Double,
        heightCm: Double,
        age: Int,
        sex: String
    ): Result<RegisterUserResponseDto> {
        return try {
            val request = RegisterUserRequestDto(
                uidUser = uidUser,
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                sex = sex
            )
            
            val response = apiService.registerUser(request)
            if (response.code() == 201) {
                // 201 Created: Usuario registrado exitosamente
                val registerResponse = RegisterUserResponseDto(message = "User registered successfully")
                Result.success(registerResponse)
            } else if (response.code() == 400) {
                // 400: Error de validación (campos incorrectos)
                Result.failure(Exception("Validation error"))
            } else {
                Result.failure(Exception("Register user failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
