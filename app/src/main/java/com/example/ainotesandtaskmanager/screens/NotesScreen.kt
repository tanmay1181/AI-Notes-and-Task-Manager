package com.example.ainotesandtaskmanager.screens

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ainotesandtaskmanager.model.Note
import com.example.ainotesandtaskmanager.ui.theme.AINotesAndTaskManagerTheme
import com.example.ainotesandtaskmanager.ui.viewmodel.NotesViewModel

@Composable
fun NotesScreen(notesViewModel: NotesViewModel, modifier: Modifier){
    val noteUiState = notesViewModel.noteUiState.collectAsState().value
    val notes = noteUiState.notes
    val showDialog = notesViewModel.showDialog.collectAsState().value
    val currentNote = noteUiState.currentNote
    val isSummarizing = noteUiState.isSummarizing

    var showBottomSheet by remember { mutableStateOf(false) }
    var showNoteEditableDialog by remember { mutableStateOf(false) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(notes, key = {it.id}) { note ->
            AnimatedVisibility(
                visible = !note.deletable,
                exit = fadeOut(animationSpec = tween(durationMillis = 800)) +
                        shrinkVertically(
                            animationSpec = tween(durationMillis = 800),
                            shrinkTowards = Alignment.Top
                        )
            ){
                DisposableEffect(Unit) {
                    onDispose {
                        if(note.deletable){
                            notesViewModel.deleteNote(note)
                            notesViewModel.updateCurrentNote(null)
                        }
                    }
                }

                NoteCard(note = note,
                    onNoteClick = {
                        notesViewModel.updateCurrentNote(note)
                        showBottomSheet = true
                    },
                    onNoteEdit = {
                        notesViewModel.updateCurrentNote(note)
                        showNoteEditableDialog = true
                    },
                    modifier = modifier
                )
            }

        }
    }

    if(showDialog){
        AddNoteDialog(
            onDismiss = {notesViewModel.hideDialog()},
            onAddNote = {
                notesViewModel.addNote(it)
            },
            modifier = modifier
        )
    }

    if(showBottomSheet){
        NoteBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                notesViewModel.updateCurrentNote(null)
            },
            onNoteEdit = {
                showBottomSheet = false
                showNoteEditableDialog = true
            },
            onNoteDelete = {
                val deletableTask = currentNote.copy(deletable = true)
                notesViewModel.updateNote(deletableTask)
                notesViewModel.updateCurrentNote(null)
                showBottomSheet = false
            },
            onSummarizeNote = {
                notesViewModel.summarizeNote()
            },
            isSummarizing = isSummarizing,
            note = currentNote!!,
            modifier = modifier

        )
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditableDialog(note: Note,
                       onDismiss: () -> Unit,
                       onConfirm: (note: Note) -> Unit,
                       modifier: Modifier){
    var noteTitle by remember { mutableStateOf(note.title) }
    var noteContent by remember {mutableStateOf(note.content)}

    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Surface(modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation)  {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(16.dp)
            ) {
                Text(text = "Update Note")

                OutlinedTextField(value = noteTitle,
                    onValueChange = {
                        noteTitle = it
                    },
                    readOnly = false,
                    label =  {
                        Text(text = "Title")
                    }
                )

                OutlinedTextField(value = noteContent,
                    onValueChange = {
                        noteContent = it
                    },
                    readOnly = false,
                    label =  {
                        Text(text = "Content")
                    },
                    modifier = modifier.height(160.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    NoteAppButton(
                        onButtonClick = {
                            onDismiss()
                        },
                        buttonIdentifier = "Cancel"
                    )

                    NoteAppButton(
                        onButtonClick = {
                            if(noteTitle.isNotBlank()){
                                val updatedNote = note.copy(title = noteTitle,
                                    content = noteContent,
                                    createdAt = note.createdAt,
                                    updatedAt = System.currentTimeMillis())
                                if(updatedNote.title != note.title ||
                                    updatedNote.content != note.content){
                                    onConfirm(updatedNote)
                                }
                                onDismiss()
                            }
                        },
                        buttonIdentifier = "Update"
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: Note,
             onNoteClick: () -> Unit,
             onNoteEdit: () -> Unit,
             modifier: Modifier){
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onNoteClick()
            })
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier.fillMaxWidth()
            ) {
                Text(text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                        .weight(1f)
                        .padding(end = 16.dp))
                //Add icons
                IconButton(onClick = {
                    onNoteEdit()
                }) {
                    Icon(imageVector = Icons.Filled.Create,
                        contentDescription = null)
                }
            }
            Column(
                modifier = modifier
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                if(note.content.isNotBlank()){
                    Text(text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis)
                }
            }
            Text(text = formatDate(note.updatedAt),
                style = MaterialTheme.typography.titleSmall)

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(onDismiss: () -> Unit,
                  onAddNote: (note: Note) -> Unit,
                  modifier: Modifier){
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        modifier = modifier.fillMaxSize(),
    ) {
        Surface(modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()
            .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            tonalElevation = AlertDialogDefaults.TonalElevation)  {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier.padding(16.dp)
            ) {
                Text(text = "Add Note")

                OutlinedTextField(value = noteTitle,
                    onValueChange = {
                        noteTitle = it
                    },
                    readOnly = false,
                    label =  {
                        Text(text = "Title")
                    }
                )

                OutlinedTextField(value = noteContent,
                    onValueChange = {
                        noteContent = it
                    },
                    readOnly = false,
                    label =  {
                        Text(text = "Content")
                    },
                    modifier = modifier.height(160.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    NoteAppButton(
                        onButtonClick = {
                            onDismiss()
                        },
                        buttonIdentifier = "Cancel"
                    )

                    NoteAppButton(
                        onButtonClick = {
                            if(noteTitle.isNotBlank()){
                                val note = Note(title = noteTitle,
                                    content = noteContent,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis())
                                onAddNote(note)
                                onDismiss()
                            }
                        },
                        buttonIdentifier = "Add"
                    )
                }
            }
        }
    }
}

@Composable
fun NoteBottomTextHierarchy(note: Note, modifier: Modifier){
    Text(text = "Title: ")
    Text(text = note.title)
    Spacer(modifier.height(16.dp))
    if(note.content.isNotBlank()){
        Text(text = "Content: ")
        Text(text = note.content)
        Spacer(modifier.height(16.dp))
    }
    Text(text = "Created At: " + formatDate(note.createdAt))
    Spacer(modifier.height(16.dp))
    Text(text = "Updated At: " + formatDate(note.updatedAt))
    if(note.summary != null){
        Spacer(modifier.height(16.dp))
        Text(text = "Summary: ")
        Text(text = note.summary)
    }
}

@Composable
fun NoteBottomButtonHierarchy(note: Note,
                              onDismissRequest: () -> Unit,
                              onNoteEdit: () -> Unit,
                              onNoteDelete: () -> Unit,
                              onSummarizeNote: (updatedNote: Note) -> Unit,
                              yOffset: Dp,
                              alpha: Float,
                              isSummarizing: Boolean,
                              modifier: Modifier){
    FloatingActionButton(
        onClick = {
            onNoteEdit()
        },
        modifier = modifier
            .padding(16.dp)
            .offset(y = yOffset)
            .alpha(alpha)
    ) {
        Icon(Icons.Default.Create, contentDescription = "note edit")
    }

    FloatingActionButton(
        onClick = {
            onSummarizeNote(note)
        },
        modifier = modifier
            .padding(16.dp)
            .offset(y = yOffset)
            .alpha(alpha)
    ) {
        if(isSummarizing){
            CircularProgressIndicator()
        }
        else{
            Icon(Icons.Default.Star, contentDescription = "note summarize")
        }
    }

    FloatingActionButton(
        onClick = {
            onNoteDelete()
            onDismissRequest()
        },
        modifier = modifier
            .padding(16.dp)
            .offset(y = yOffset)
            .alpha(alpha)
    ) {
        Icon(Icons.Default.Delete, contentDescription = "note deleted")
    }
}

@Preview
@Composable
fun NotePreview(){
    AINotesAndTaskManagerTheme {

    }
}