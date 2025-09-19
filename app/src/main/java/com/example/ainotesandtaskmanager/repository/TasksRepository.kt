package com.example.ainotesandtaskmanager.repository

import com.example.ainotesandtaskmanager.model.Task
import com.example.ainotesandtaskmanager.model.TaskDao
import kotlinx.coroutines.flow.Flow

interface TasksRepository{
    suspend fun insert(task: Task): Long
    suspend fun delete(task: Task)
    suspend fun update(task: Task)
    fun getTasks(): Flow<List<Task>>
}

class OfflineTasksRepository(val taskDao: TaskDao): TasksRepository{
    override suspend fun insert(task: Task) = taskDao.insert(task)

    override suspend fun delete(task: Task) = taskDao.delete(task)

    override suspend fun update(task: Task) = taskDao.update(task)

    override fun getTasks(): Flow<List<Task>> = taskDao.getTasks()

}