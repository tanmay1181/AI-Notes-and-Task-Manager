package com.example.ainotesandtaskmanager.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ainotesandtaskmanager.model.BaseModel
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.ui.viewmodel.NotesViewModel
import com.example.ainotesandtaskmanager.ui.viewmodel.TasksViewModel

@Composable
fun HomeScreen(tasksViewModel: TasksViewModel,
               notesViewModel: NotesViewModel,
               modifier: Modifier){
    // Task Variables
    val taskUiState = tasksViewModel.taskUiState.collectAsState().value
    val tasks = taskUiState.tasks
    val currentTask = taskUiState.currentTask

    // Note Variables
    val noteUiState = notesViewModel.noteUiState.collectAsState().value
    val notes = noteUiState.notes
    val currentNote = noteUiState.currentNote
    val isSummarizing = noteUiState.isSummarizing
    var showNoteEditableDialog by remember { mutableStateOf(false) }

    // Common Variables
    var showBottomSheet by remember { mutableStateOf(false) }
    val baseModels = tasks + notes

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        HomeCard(
            baseModels = baseModels,
            onCardClick = { model ->
                when(model){
                    is Note -> {notesViewModel.updateCurrentNote(model)}
                    is Task -> {tasksViewModel.updateCurrentTask(model)}
                }
                showBottomSheet = true
            },
            onNoteEdit = { note ->
                notesViewModel.updateCurrentNote(note)
                showBottomSheet = false
                showNoteEditableDialog = true
            },
            onCompletedTask = { task ->
                val updatedTask = task.copy(isCompleted = true)
                tasksViewModel.updateTask(updatedTask)
                tasksViewModel.deleteTask(updatedTask)
            },
            modifier = modifier
        )
    }

    if(showBottomSheet){
        if(currentNote != null){
            NoteBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    notesViewModel.updateCurrentNote(null)
                },
                onNoteEdit = {
                    notesViewModel.updateCurrentNote(currentNote)
                    showBottomSheet = false
                    showNoteEditableDialog = true
                },
                onNoteDelete = {
                        val deletableNote = currentNote.copy(deletable = true)
                        notesViewModel.updateNote(deletableNote)
                        notesViewModel.deleteNote(deletableNote)
                        notesViewModel.updateCurrentNote(null)
                        showBottomSheet = false
                },
                onSummarizeNote = {
                    notesViewModel.summarizeNote()
                },
                isSummarizing = isSummarizing,
                note = currentNote,
                modifier = modifier
            )
        }
        else{
            TaskBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    tasksViewModel.updateCurrentTask(null)
                },
                onTaskCompleted = {
                    val updatedTask = currentTask.copy(isCompleted = true)
                    tasksViewModel.updateTask(updatedTask)
                    tasksViewModel.deleteTask(updatedTask)
                    tasksViewModel.updateCurrentTask(null)
                },
                onTaskDelete = {
                    val deletableTask = currentTask.copy(isCompleted = true)
                    tasksViewModel.updateTask(deletableTask)
                    tasksViewModel.deleteTask(deletableTask)
                    tasksViewModel.updateCurrentTask(null)
                    showBottomSheet = false
                },
                task = currentTask!!,
                modifier = modifier
            )
        }
    }

    // Only for Note Entity
    if(showNoteEditableDialog){
        NoteEditableDialog(
            note = currentNote!!,
            onDismiss = {
                notesViewModel.updateCurrentNote(null)
                showNoteEditableDialog = false
            },
            onConfirm = {
                notesViewModel.updateNote(it)
                showNoteEditableDialog = false
                notesViewModel.updateCurrentNote(null)
            },
            modifier = modifier
        )
    }
}

@Composable
fun HomeCard(baseModels: List<BaseModel>,
             onCardClick: (baseModel: BaseModel) -> Unit,
             onNoteEdit: (note: Note) -> Unit,
             onCompletedTask: (task: Task) -> Unit,
             modifier: Modifier){
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(baseModels){baseModel ->
            when(baseModel){
                is Note -> NoteCard(
                    note = baseModel,
                    onNoteClick = {onCardClick(baseModel)},
                    onNoteEdit = {onNoteEdit(baseModel)},
                    modifier = modifier
                )
                is Task -> TaskCard(
                    task = baseModel,
                    onCompletedTask = {onCompletedTask(baseModel)},
                    onTaskClick = {onCardClick(baseModel)},
                    modifier = modifier
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteBottomSheet(
    onDismissRequest: () -> Unit,
    onNoteEdit: () -> Unit,
    onNoteDelete: () -> Unit,
    onSummarizeNote: (updatedNote: Note) -> Unit,
    isSummarizing: Boolean,
    note: Note,
    modifier: Modifier
) {
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var isScrollingUp by remember { mutableStateOf(true) }

    // Detect scroll direction
    LaunchedEffect(listState) {
        var prevIndex = listState.firstVisibleItemIndex
        var prevOffset = listState.firstVisibleItemScrollOffset

        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isScrollingUp = if (index == prevIndex) {
                    offset <= prevOffset
                } else {
                    index <= prevIndex
                }
                prevIndex = index
                prevOffset = offset
            }
    }

    ModalBottomSheet(
        onDismissRequest = {onDismissRequest()},
        sheetState = sheetState
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 400.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, bottom = 96.dp, end = 16.dp)
            ) {
                item{
                    NoteBottomTextHierarchy(note, modifier)

                }
            }

            // Always show if list is not scrollable
            val showFab = isScrollingUp || (!listState.canScrollForward && !listState.canScrollBackward)

            val alpha by animateFloatAsState(if (showFab) 1f else 0f, animationSpec = tween(400))
            val yOffset by animateDpAsState(if (showFab) 0.dp else 72.dp, animationSpec = tween(400))

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)) {
                NoteBottomButtonHierarchy(
                    note = note,
                    onDismissRequest = { onDismissRequest() },
                    onNoteEdit = { onNoteEdit() },
                    onNoteDelete = { onNoteDelete() },
                    onSummarizeNote = { onSummarizeNote(note) },
                    yOffset = yOffset,
                    alpha = alpha,
                    isSummarizing = isSummarizing,
                    modifier = modifier
                )

            }
        }
    }
}
