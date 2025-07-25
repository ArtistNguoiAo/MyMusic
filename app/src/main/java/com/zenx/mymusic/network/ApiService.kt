package com.zenx.mymusic.network

import com.zenx.mymusic.model.Song
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("songs")
    suspend fun getSongs(): Response<List<Song>>
}

object ApiClient {
    private const val BASE_URL = "http://194.233.76.208:8001/"
    
    private val gson = com.google.gson.GsonBuilder()
        .setLenient()
        .create()
    
    val instance: ApiService by lazy {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
            
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create(gson))
            .build()
            
        retrofit.create(ApiService::class.java)
    }
}
