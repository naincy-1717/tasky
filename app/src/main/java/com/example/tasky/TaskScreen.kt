package com.example.tasky

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

    // 1. Connect Room Database instead of SharedPreferences
    val dao = remember { AppDatabase.getDatabase(context).taskDao() }
    val coroutineScope = rememberCoroutineScope()

    // 2. Fetch live data from Room (this automatically triggers your smart sorting query!)
    val tasks by dao.getTasksByUser(userEmail).collectAsState(initial = emptyList())
    var newTaskText by remember { mutableStateOf("") }

    var selectedPopupScreen by remember { mutableStateOf<String?>(null) }

    // Palette
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Screen Header
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
                            val textToSave = newTaskText
                            newTaskText = ""

                            // 3. Send the captured text straight to the Room database
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

            // Task List Area
            Box(modifier = Modifier.weight(1f)) {
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
                                    // Checkbox
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { isChecked ->
                                            // 4. Update the exact task in Room
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

                                    // Task Text
                                    Text(
                                        text = task.title,
                                        color = if (task.isDone) Color.Gray else Color.White,
                                        fontSize = 16.sp,
                                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp)
                                    )

                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            // 5. Delete from Room database
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

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Row with 4 Circles (C, T, S, A)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    "Calendar" to "C",
                    "Timer" to "T",
                    "Stopwatch" to "S",
                    "Account" to "A"
                )

                navItems.forEach { (name, initial) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedPopupScreen = name }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(neonPurple, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = darkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            color = lightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Pop-up Screen Dialogs
        if (selectedPopupScreen != null) {
            when (selectedPopupScreen) {
                "Calendar" -> CalendarDialog(onDismissRequest = { selectedPopupScreen = null })
                "Timer" -> TimerDialog(onDismissRequest = { selectedPopupScreen = null })
                "Stopwatch" -> StopwatchDialog(onDismissRequest = { selectedPopupScreen = null })
                "Account" -> AccountDialog(userEmail = userEmail, onDismissRequest = { selectedPopupScreen = null })
            }
        }
    }
}