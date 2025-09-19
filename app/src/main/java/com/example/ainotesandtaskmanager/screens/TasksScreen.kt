package com.example.ainotesandtaskmanager.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.ui.viewmodel.TasksViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(tasksViewModel: TasksViewModel,
                modifier: Modifier){
    val taskUiState = tasksViewModel.taskUiState.collectAsState().value
    val tasks = taskUiState.tasks
    val currentTask = taskUiState.currentTask
    val showDialog = tasksViewModel.showDialog.collectAsState().value
    var showBottomSheet by remember { mutableStateOf(false) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
    ){
        items(tasks, key = { it.id }) { task ->
            AnimatedVisibility(
                visible = !task.isCompleted,
                exit = fadeOut(animationSpec = tween(durationMillis = 800)) +
                        shrinkVertically(
                            animationSpec = tween(durationMillis = 800),
                            shrinkTowards = Alignment.Top
                        )
            ) {
                DisposableEffect(task) {
                    onDispose {
                        if(task.isCompleted){
                            tasksViewModel.deleteTask(task)
                            tasksViewModel.updateCurrentTask(null)
                        }
                    }
                }

                TaskCard(task = task,
                    onCompletedTask = { updatedTask ->
                        tasksViewModel.updateTask(updatedTask)
                    },
                    onTaskClick = {
                        tasksViewModel.updateCurrentTask(task)
                        showBottomSheet = true
                    },
                    modifier = modifier
                )
            }
        }
    }

    if(showBottomSheet){
        TaskBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                tasksViewModel.updateCurrentTask(null)
            },
            onTaskCompleted = {
                val updatedTask = currentTask.copy(isCompleted = true)
                tasksViewModel.updateTask(updatedTask)
                tasksViewModel.updateCurrentTask(null)
            },
            onTaskDelete = {
                val deletableTask = currentTask.copy(isCompleted = true)
                tasksViewModel.updateTask(deletableTask)
                tasksViewModel.updateCurrentTask(null)
                showBottomSheet = false
            },
            task = currentTask!!,
            modifier = modifier
        )
    }

    if(showDialog){
        val context = LocalContext.current
        AddTaskDialog(
            onDismiss = { tasksViewModel.hideDialog() },
            modifier = modifier,
            onAddTask = {
                tasksViewModel.addTask(it)
            }
        )
    }
}

@Composable
fun TaskCard(task: Task,
             onTaskClick: () -> Unit,
             onCompletedTask: (task: Task) -> Unit,
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
            .clickable(
                onClick = {
                    onTaskClick()
                }
            )
    ) {
        Column(
            modifier = modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = modifier
                        .weight(1f)
                        .padding(end = 16.dp))

                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = {
                        if(!task.isCompleted){
                            val updatedTask = task.copy(isCompleted = it)
                            onCompletedTask(updatedTask)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = Color.White
                    ),
                    modifier = modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
            }
            if(task.dueDate != null){
                Text(text = formatDate(task.dueDate),
                    style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(onDismiss: () -> Unit,
                  onAddTask: (task: Task) -> Unit,
                  modifier: Modifier){
    var taskTitle by remember { mutableStateOf("") }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var dueDateTime by remember { mutableStateOf<Long?>(0L) }

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
                Text(text = "Add Task")

                OutlinedTextField(value = taskTitle,
                    onValueChange = {
                        taskTitle = it
                    },
                    readOnly = false,
                    label =  {
                        Text(text = "Title")
                    })

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(value = dueDateTime?.let { formatDate(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        textStyle = MaterialTheme.typography.labelSmall,
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = {dueDateTime = null}
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Date")
                                }

                                IconButton(
                                    onClick = {showDateTimePicker = true}
                                ) {
                                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Choose Date")
                                }
                            }
                        }
                    )
                }

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
                            if(isValidTask(taskTitle, dueDateTime)){
                                val task = Task(title = taskTitle,
                                    dueDate = dueDateTime,
                                    createdAt = System.currentTimeMillis())
                                onAddTask(task)
                                onDismiss()
                            }
                        },
                        buttonIdentifier = "Add"
                    )
                }
            }
        }
    }

    if(showDateTimePicker){
        DateTimePickerDialog(
            onDateTimeSelected = {
                dueDateTime = it
            },
            onDismiss = {
                showDateTimePicker = false
            }
        )
    }
}

@Composable
fun NoteAppButton(onButtonClick: () -> Unit,
                  buttonIdentifier: String){
    Button(
        onClick = {
            onButtonClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.onPrimary),
        elevation = ButtonDefaults.buttonElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = buttonIdentifier)
    }
}

fun formatDate(timestamp: Long?): String {
    if(timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun isValidTask(title: String, validDateTime: Long?): Boolean {
    return title.isNotBlank() &&
            (validDateTime == null || validDateTime > System.currentTimeMillis())
}

@Composable
fun DateTimePickerDialog(
    onDateTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Create DatePickerDialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // After selecting date → show TimePicker
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)

                    onDateTimeSelected(calendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).apply {
                setOnDismissListener { onDismiss() } // dismiss if user cancels time picker
            }.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setOnDismissListener { onDismiss() } // dismiss if user cancels date picker
    }

    datePickerDialog.show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBottomSheet(
    onDismissRequest: () -> Unit,
    onTaskCompleted: () -> Unit,
    onTaskDelete: () -> Unit,
    task: Task,
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
                    TaskBottomTextHierarchy(task, modifier)
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
                TaskBottomButtonHierarchy(
                    onDismissRequest = { onDismissRequest() },
                    onTaskCompleted = { onTaskCompleted() },
                    onTaskDelete = {onTaskDelete()},
                    yOffset = yOffset,
                    alpha = alpha,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun TaskBottomTextHierarchy(task: Task, modifier: Modifier){
    Text(text = "Title: ")
    Text(text = task.title)
    Spacer(modifier.height(16.dp))
    Text(text = "Created At: " + formatDate(task.createdAt))
    Spacer(modifier.height(16.dp))
    Text(text = "Due Date: " + formatDate(task.dueDate))
}

@Composable
fun TaskBottomButtonHierarchy(onDismissRequest: () -> Unit,
                              onTaskCompleted: () -> Unit,
                              onTaskDelete: () -> Unit,
                              yOffset: Dp,
                              alpha: Float,
                              modifier: Modifier){
    FloatingActionButton(
        onClick = {
            onTaskCompleted()
            onDismissRequest()
        },
        modifier = modifier
            .padding(16.dp)
            .offset(y = yOffset)
            .alpha(alpha)
    ) {
        Icon(Icons.Default.Check, contentDescription = "task completed")
    }
    FloatingActionButton(
        onClick = {
            onTaskDelete()
            onDismissRequest()
        },
        modifier = modifier
            .padding(16.dp)
            .offset(y = yOffset)
            .alpha(alpha)
    ) {
        Icon(Icons.Default.Delete, contentDescription = "task deleted")
    }
}
