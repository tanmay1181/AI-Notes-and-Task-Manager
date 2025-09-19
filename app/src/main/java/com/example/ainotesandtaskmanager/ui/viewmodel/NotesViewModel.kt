package com.example.ainotesandtaskmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ainotesandtaskmanager.model.BaseModel
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.repository.AiRepository
import com.example.ainotesandtaskmanager.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotesViewModel(val notesRepository: NotesRepository,
                     private val aiRepository: AiRepository): ViewModel() {
    private val _noteUiState = MutableStateFlow<NoteUiState>(NoteUiState())
    val noteUiState: StateFlow<NoteUiState> = _noteUiState

    val showDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            notesRepository.getNotes().collect { notes ->
                _noteUiState.value = _noteUiState.value.copy(notes = notes)
            }
        }
    }

    fun summarizeNote(){
        viewModelScope.launch {
            _noteUiState.value.let { noteUiState ->
                _noteUiState.value = _noteUiState.value.copy(isSummarizing = true)   // start loading
                try {
                    val currentNote = noteUiState.currentNote
                    val content = currentNote?.content
                    val summary = aiRepository.summarize(content?:"")
                    val updatedNote = currentNote?.copy(
                        summary = summary,
                        updatedAt = System.currentTimeMillis()
                    )

                    if(updatedNote != null) notesRepository.update(updatedNote)

                    _noteUiState.value = _noteUiState.value.copy(currentNote = updatedNote)
                } finally {
                    _noteUiState.value = _noteUiState.value.copy(isSummarizing = false) // stop loading
                }
            }
        }
    }

    fun updateCurrentNote(note: Note?){
        _noteUiState.value = _noteUiState.value.copy(currentNote = note)
    }

    fun addNote(note: Note){
        viewModelScope.launch {
            notesRepository.insert(note)
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            notesRepository.delete(note)
        }
    }

    fun updateNote(note: Note){
        viewModelScope.launch {
            notesRepository.update(note)
        }
    }

    fun showDialog(){
        showDialog.value = true
    }

    fun hideDialog(){
        showDialog.value = false
    }
}

data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val currentNote: Note? = null,
    val isSummarizing: Boolean = false
)