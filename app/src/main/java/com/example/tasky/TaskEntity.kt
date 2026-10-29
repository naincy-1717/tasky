package com.example.tasky // Make sure this matches your actual package name

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isDone: Boolean = false,
    val userEmail: String // This ensures we can filter tasks by which user is logged in
)