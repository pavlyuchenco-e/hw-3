package com.example.hw3

import com.example.hw3.data.remote.ShowsApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    private const val BASE_URL = "https://api.tvmaze.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)   // ← максимум 30 секунд на соединение
        .readTimeout(30, TimeUnit.SECONDS)      // ← максимум 30 секунд на чтение
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ShowsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ShowsApi::class.java)
}