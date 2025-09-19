package com.example.ainotesandtaskmanager.data.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

data class Part(val text: String)
data class Content(val parts: List<Part>)
data class GeminiRequest(val contents: List<Content>)

data class Candidate(val content: Content)
data class GeminiResponse(val candidates: List<Candidate>)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun getChatCompletion(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}