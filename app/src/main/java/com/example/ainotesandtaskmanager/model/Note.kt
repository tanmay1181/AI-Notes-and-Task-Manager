package com.example.ainotesandtaskmanager.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    override val id: Int = 0,
    override val title: String,
    val content: String,
    val summary: String? = null,
    val deletable: Boolean = false,
    override val createdAt: Long,
    val updatedAt: Long
): BaseModel(id, title, createdAt)
