package com.example.ainotesandtaskmanager.repository

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.example.ainotesandtaskmanager.data.api.Content
import com.example.ainotesandtaskmanager.data.api.GeminiClient
import com.example.ainotesandtaskmanager.data.api.GeminiRequest
import com.example.ainotesandtaskmanager.data.api.Part
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.model.NoteDao
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.notification.NotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface NotesRepository {
    suspend fun insert(note: Note)
    suspend fun delete(note: Note)
    suspend fun update(note: Note)
    fun getNotes(): Flow<List<Note>>
}

class OfflineNotesRepository(val notesDao: NoteDao): NotesRepository{
    override suspend fun insert(note: Note) = notesDao.insert(note)

    override suspend fun delete(note: Note) = notesDao.delete(note)

    override suspend fun update(note: Note) = notesDao.update(note)

    override fun getNotes(): Flow<List<Note>> = notesDao.getNotes()

}

class AiRepository {
    suspend fun summarize(noteContent: String): String = withContext(Dispatchers.IO) {
        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part("Summarize this note into 2-3 bullet points: $noteContent")))
            )
        )

        try {
            val response = GeminiClient.api.getChatCompletion(
                apiKey = "AIzaSyCjo22EUCZWgVXu9kwBR7a04ywnDRdLX2Q", // pass it here
                request = request
            )

            // Extract the first text part of the first candidate
            response.candidates.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: "No summary available."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}