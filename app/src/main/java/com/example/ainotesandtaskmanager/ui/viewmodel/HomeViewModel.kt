package com.example.ainotesandtaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.ainotesandtaskmanager.model.BaseModel
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel: ViewModel() {
    private val _currentModel = MutableStateFlow<BaseModel?>(null)
    val currentModel: StateFlow<BaseModel?> = _currentModel

    fun setCurrentModel(baseModel: BaseModel){
        when(baseModel){
            is Note -> {_currentModel.value = baseModel
            }
            is Task -> {_currentModel.value = baseModel

            }
        }
    }
}