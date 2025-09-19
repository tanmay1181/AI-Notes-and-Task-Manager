package com.example.ainotesandtaskmanager.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object GeminiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: GeminiApiService = retrofit.create(GeminiApiService::class.java)
}