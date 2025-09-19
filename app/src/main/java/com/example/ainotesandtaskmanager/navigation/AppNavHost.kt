package com.example.ainotesandtaskmanager.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.example.ainotesandtaskmanager.screens.HomeScreen
import com.example.ainotesandtaskmanager.screens.NotesScreen
import com.example.ainotesandtaskmanager.screens.SettingsScreen
import com.example.ainotesandtaskmanager.screens.TasksScreen
import com.example.ainotesandtaskmanager.screens.util.Screen
import com.example.ainotesandtaskmanager.ui.viewmodel.NotesViewModel
import com.example.ainotesandtaskmanager.ui.viewmodel.TasksViewModel

@Composable
fun AppNavHost(navController: NavHostController,
               notesViewModel: NotesViewModel,
               tasksViewModel: TasksViewModel,
               modifier: Modifier,
               innerPadding: PaddingValues){
    NavHost(navController = navController,
        startDestination = Screen.HomeScreen.route,
        modifier = modifier.padding(innerPadding)){
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(tasksViewModel, notesViewModel, modifier = modifier)
        }

        composable(route = Screen.TasksScreen.route,
            deepLinks = listOf(navDeepLink { uriPattern = "myapp://tasks" })) {
            TasksScreen(tasksViewModel, modifier)
        }

        composable(route = Screen.NotesScreen.route) {
            NotesScreen(notesViewModel, modifier)
        }

        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(modifier)
        }
    }
}