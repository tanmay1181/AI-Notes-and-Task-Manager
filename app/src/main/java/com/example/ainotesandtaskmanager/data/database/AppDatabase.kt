package com.example.ainotesandtaskmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.model.NoteDao
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.model.TaskDao

@Database(entities = [Note::class, Task::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getNoteDao(): NoteDao
    abstract fun getTaskDao(): TaskDao

    companion object{
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "ai_notes_database"
                ).build().also { Instance = it }
            }
        }
    }
}