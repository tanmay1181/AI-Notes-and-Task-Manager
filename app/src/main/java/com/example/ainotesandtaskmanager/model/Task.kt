package com.example.ainotesandtaskmanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    override val id: Int = 0,
    override val title: String,
    val isCompleted: Boolean = false,
    val dueDate: Long? = null,
    override val createdAt: Long
): BaseModel(id, title, createdAt)
