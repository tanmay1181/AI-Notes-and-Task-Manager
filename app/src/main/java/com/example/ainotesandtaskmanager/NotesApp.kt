package com.example.ainotesandtaskmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.navigation.AppNavHost
import com.example.ainotesandtaskmanager.screens.util.Screen
import com.example.ainotesandtaskmanager.ui.viewmodel.AppViewModelProvider
import com.example.ainotesandtaskmanager.ui.viewmodel.NotesViewModel
import com.example.ainotesandtaskmanager.ui.viewmodel.TasksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesApp(){
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry.value?.destination?.route ?: "home"
    val screens = listOf(Screen.HomeScreen, Screen.TasksScreen, Screen.NotesScreen, Screen.SettingsScreen)
    val notesViewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val tasksViewModel: TasksViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val modifier = Modifier

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = getScreenTitle(currentRoute, screens) )
                },
                navigationIcon = {

                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = modifier.fillMaxWidth()
                ) {
                    screens.forEach {screen ->
                        IconButton(
                            onClick = {
                                navController.navigate(screen.route)
                            }
                        ) {
                            val isSelected = isSelectedBottomNavIcon(screen.route, currentRoute)
                            Icon(painter = painterResource(screen.icon) ,
                                contentDescription = screen.title,
                                tint = if(isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if(showFAB(currentRoute)){
                IconButton(
                    onClick = {
                        when(currentRoute){
                            Screen.NotesScreen.route -> {
                                notesViewModel.showDialog()
                            }
                            Screen.TasksScreen.route ->{
                                tasksViewModel.showDialog()
                            }
                            else -> {}
                        }
                    },
                    modifier = modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.onBackground)
                ) {
                    Icon(imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.background)
                }
            }

        }
    ) {innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = modifier,
            innerPadding = innerPadding,
            notesViewModel = notesViewModel,
            tasksViewModel = tasksViewModel
        )
    }


}

fun isSelectedBottomNavIcon(route: String, currentRoute: String): Boolean{
    return route == currentRoute
}

fun showFAB(screenRoute: String): Boolean{
    return screenRoute == Screen.NotesScreen.route || screenRoute == Screen.TasksScreen.route
}

fun getScreenTitle(route: String, screens: List<Screen>): String{
    screens.forEach { screen ->
        if(screen.route == route){
            return screen.title
        }
    }
    return "Home"
}