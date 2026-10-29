package com.example.tasky // Ensure this matches your package

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(userEmail: String = "default_user") {
    val context = LocalContext.current

    // Connect to the database and DAO
    val dao = remember { AppDatabase.getDatabase(context).taskDao() }

    // Coroutine scope is needed to run database tasks in the background
    val coroutineScope = rememberCoroutineScope()

    // Collect tasks from Room database in real-time
    val tasks by dao.getTasksByUser(userEmail).collectAsState(initial = emptyList())
    var newTaskText by remember { mutableStateOf("") }

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

        Text(
            text = "My Tasks",
            color = neonPurple,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Logged in as: $userEmail",
            color = Color.Gray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

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
                        // 1. Capture the exact text right now
                        val textToSave = newTaskText

                        // 2. Clear the UI text box instantly
                        newTaskText = ""

                        // 3. Send the captured text to the database
                        coroutineScope.launch {
                            dao.insertTask(
                                TaskEntity(
                                    title = textToSave,
                                    isDone = false,
                                    userEmail = userEmail
                                )
                            )
                        }
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
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = { isChecked ->
                                    // Update database when checked/unchecked
                                    coroutineScope.launch {
                                        dao.updateTask(task.copy(isDone = isChecked))
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = neonPurple,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = darkBackground
                                )
                            )

                            Text(
                                text = task.title,
                                color = if (task.isDone) Color.Gray else Color.White,
                                fontSize = 16.sp,
                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            )

                            IconButton(
                                onClick = {
                                    // Delete from database
                                    coroutineScope.launch {
                                        dao.deleteTask(task)
                                    }
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