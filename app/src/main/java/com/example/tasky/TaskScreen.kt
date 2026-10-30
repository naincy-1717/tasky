package com.example.tasky

import android.content.Context
import androidx.compose.foundation.background
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

data class TaskItem(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

@Composable
fun TaskScreen(userEmail: String = "default_user") {
    val context = LocalContext.current
    val prefs = remember(userEmail) {
        context.getSharedPreferences("TaskyTasks_$userEmail", Context.MODE_PRIVATE)
    }

    fun loadUserTasks(): List<TaskItem> {
        val savedSet = prefs.getStringSet("tasks_list", emptySet()) ?: emptySet()
        return savedSet.mapNotNull { raw ->
            val parts = raw.split("|||")
            if (parts.size == 3) {
                TaskItem(id = parts[0].toInt(), title = parts[1], isDone = parts[2].toBoolean())
            } else null
        }.sortedBy { it.id }
    }

    fun saveUserTasks(taskList: List<TaskItem>) {
        val stringSet = taskList.map { "${it.id}|||${it.title}|||${it.isDone}" }.toSet()
        prefs.edit().putStringSet("tasks_list", stringSet).apply()
    }

    var tasks by remember { mutableStateOf(loadUserTasks()) }
    var newTaskText by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf((tasks.maxOfOrNull { it.id } ?: 0) + 1) }

    var selectedPopupScreen by remember { mutableStateOf<String?>(null) }

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
                            val updatedList = tasks + TaskItem(id = nextId++, title = newTaskText)
                            tasks = updatedList
                            saveUserTasks(updatedList)
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
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { isChecked ->
                                            val updatedList = tasks.map {
                                                if (it.id == task.id) it.copy(isDone = isChecked) else it
                                            }
                                            tasks = updatedList
                                            saveUserTasks(updatedList)
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
                                            val updatedList = tasks.filter { it.id != task.id }
                                            tasks = updatedList
                                            saveUserTasks(updatedList)
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

        // Popup Dialogs delegated to their respective files
        when (selectedPopupScreen) {
            "Calendar" -> CalendarDialog(onDismissRequest = { selectedPopupScreen = null })
            "Timer" -> TimerDialog(onDismissRequest = { selectedPopupScreen = null })
            "Stopwatch" -> StopwatchDialog(onDismissRequest = { selectedPopupScreen = null })
            "Account" -> AccountDialog(userEmail = userEmail, onDismissRequest = { selectedPopupScreen = null })
        }
    }
}