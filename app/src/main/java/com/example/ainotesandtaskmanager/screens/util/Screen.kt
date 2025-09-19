package com.example.ainotesandtaskmanager.screens.util

import com.example.ainotesandtaskmanager.R

sealed class Screen(val route: String, val icon: Int, val title: String) {
    object HomeScreen: Screen("home", R.drawable.baseline_home_24 ,"Home")
    object SettingsScreen: Screen("settings", R.drawable.baseline_settings_24, "Settings")
    object TasksScreen: Screen("tasks", R.drawable.baseline_task_24, "Tasks")
    object NotesScreen: Screen("notes", R.drawable.baseline_note_24, "Notes")
}