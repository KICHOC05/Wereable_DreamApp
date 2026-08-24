package com.example.appmobile.domain.usecase

import com.example.appmobile.data.remote.model.GetUserByUidResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface GetUserByUidApiService {
    @GET("getUserByUid")
    suspend fun getUserByUid(@Query("uidUser") uidUser: String): Response<GetUserByUidResponse>
}

class GetUserByUidUseCase {
    private val apiService: GetUserByUidApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(com.example.appmobile.BuildConfig.USER_LOOKUP_API_BASE_URL)
            .client(okhttp3.OkHttpClient.Builder().addInterceptor(com.example.appmobile.data.remote.FirebaseAuthInterceptor()).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(GetUserByUidApiService::class.java)
    }
    
    suspend operator fun invoke(uidUser: String): Result<GetUserByUidResponse> {
        return try {
            val response = apiService.getUserByUid(uidUser)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Get user failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
