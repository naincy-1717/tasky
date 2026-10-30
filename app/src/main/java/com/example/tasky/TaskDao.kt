package com.example.tasky // Ensure this matches your package

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Fetches only the tasks belonging to the logged-in user in real-time
    @Query("SELECT * FROM tasks WHERE userEmail = :email ORDER BY isDone ASC, id DESC")
    fun getTasksByUser(email: String): Flow<List<TaskEntity>>

    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}