package com.example.appmobile.data.remote

import com.example.appmobile.domain.model.SleepDataUpload
import com.example.appmobile.domain.model.SleepUploadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SleepApiService {
    
    @POST("registerUserSleepData")
    suspend fun uploadSleepData(
        @Body sleepData: SleepDataUpload
    ): Response<SleepUploadResponse>
}

object SleepApiClient {
    
    // Producción Firebase Cloud Functions
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(com.example.appmobile.BuildConfig.SLEEP_API_BASE_URL)
        .client(okhttp3.OkHttpClient.Builder().addInterceptor(FirebaseAuthInterceptor()).build())
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
    
    val apiService: SleepApiService = retrofit.create(SleepApiService::class.java)
    
    // Método para obtener la URL actual (útil para debugging)
    fun getCurrentBaseUrl(): String = com.example.appmobile.BuildConfig.SLEEP_API_BASE_URL
}
