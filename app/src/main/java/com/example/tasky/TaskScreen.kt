package com.example.tasky

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TaskItem(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

@Composable
fun TaskScreen() {
    var tasks by remember { mutableStateOf(listOf<TaskItem>()) }
    var newTaskText by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(1) }

    // Color Palette
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Screen Title
        Text(
            text = "My Tasks",
            color = neonPurple,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add Task Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskText,
                onValueChange = { newTaskText = it },
                label = { Text("Enter a task...", color = lightGray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonPurple,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = {
                    if (newTaskText.isNotBlank()) {
                        tasks = tasks + TaskItem(id = nextId++, title = newTaskText)
                        newTaskText = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Task List
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tasks added yet!", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox: cuts through text when checked
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { isChecked ->
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(isDone = isChecked) else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = neonPurple,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = darkBackground
                                )
                            )

                            // Task Text (strikes through when isDone is true)
                            Text(
                                text = task.title,
                                color = if (task.isDone) Color.Gray else Color.White,
                                fontSize = 16.sp,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )

                            // Cross Button: Deletes the task
                            IconButton(
                                onClick = {
                                    tasks = tasks.filter { it.id != task.id }
                                }
                            ) {
                                Text(
                                    text = "✕",
                                    color = Color.Gray,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}