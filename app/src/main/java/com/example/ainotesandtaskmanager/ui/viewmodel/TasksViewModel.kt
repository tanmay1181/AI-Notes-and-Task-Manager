package com.example.ainotesandtaskmanager.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.repository.AlarmRepository
import com.example.ainotesandtaskmanager.repository.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TasksViewModel(val tasksRepository: TasksRepository,
                     val alarmRepository: AlarmRepository): ViewModel() {
    private val _taskUiState = MutableStateFlow<TaskUiState>(TaskUiState())
    val taskUiState: StateFlow<TaskUiState> = _taskUiState

    val showDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            tasksRepository.getTasks().collect { tasks ->
                _taskUiState.value = _taskUiState.value.copy(tasks = tasks)
            }
        }
    }

    fun updateCurrentTask(task: Task?){
        _taskUiState.value = _taskUiState.value.copy(currentTask = task)
    }

    fun addTask(task: Task){
        viewModelScope.launch {
            val newId = tasksRepository.insert(task).toInt()
            task.dueDate?.let {
                val savedTask = task.copy(id = newId)
                alarmRepository.scheduleTaskReminder(savedTask)
            }
        }
    }

    fun deleteTask(task: Task){
        viewModelScope.launch {
            alarmRepository.cancelTaskReminder(task.id)
            tasksRepository.delete(task)
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch {
            tasksRepository.update(task)
        }
    }

    fun showDialog(){
        showDialog.value = true
    }

    fun hideDialog(){
        showDialog.value = false
    }
}

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val currentTask: Task? = null,
)