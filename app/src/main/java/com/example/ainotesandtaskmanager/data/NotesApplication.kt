package com.example.ainotesandtaskmanager.data

import android.app.Application
import android.util.Log

class NotesApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}