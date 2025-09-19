package com.example.ainotesandtaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ainotesandtaskmanager.data.NotesApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            NotesViewModel(notesApplication().container.notesRepository,
                notesApplication().container.aiRepository)
        }

        initializer {
            TasksViewModel(notesApplication().container.taskRepository,
                notesApplication().container.alarmRepository)
        }
    }
}

fun CreationExtras.notesApplication(): NotesApplication =
    (this[APPLICATION_KEY] as NotesApplication)
