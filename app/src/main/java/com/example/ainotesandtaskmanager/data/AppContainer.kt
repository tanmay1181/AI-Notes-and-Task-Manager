package com.example.ainotesandtaskmanager.data

import android.content.Context
import com.example.ainotesandtaskmanager.data.database.AppDatabase
import com.example.ainotesandtaskmanager.repository.AiRepository
import com.example.ainotesandtaskmanager.repository.AlarmRepository
import com.example.ainotesandtaskmanager.repository.NotesRepository
import com.example.ainotesandtaskmanager.repository.OfflineNotesRepository
import com.example.ainotesandtaskmanager.repository.OfflineTasksRepository
import com.example.ainotesandtaskmanager.repository.TasksRepository

interface AppContainer {
    val notesRepository: NotesRepository
    val taskRepository: TasksRepository
    val aiRepository: AiRepository
    val alarmRepository: AlarmRepository
}

class AppDataContainer(context: Context): AppContainer{
    val appDatabase = AppDatabase.getDatabase(context)

    override val notesRepository: NotesRepository by lazy {
        OfflineNotesRepository(appDatabase.getNoteDao())
    }

    override val taskRepository: TasksRepository by lazy {
        OfflineTasksRepository(appDatabase.getTaskDao())
    }

    override val aiRepository: AiRepository by lazy {
        AiRepository()
    }

    override val alarmRepository: AlarmRepository by lazy {
        AlarmRepository(context)
    }
}