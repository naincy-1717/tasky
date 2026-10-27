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
import android.widget.CalendarView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.border
import androidx.compose.runtime.mutableIntStateOf

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

    // Helper to load user's saved tasks
    fun loadUserTasks(): List<TaskItem> {
        val savedSet = prefs.getStringSet("tasks_list", emptySet()) ?: emptySet()
        return savedSet.mapNotNull { raw ->
            val parts = raw.split("|||")
            if (parts.size == 3) {
                TaskItem(id = parts[0].toInt(), title = parts[1], isDone = parts[2].toBoolean())
            } else null
        }.sortedBy { it.id }
    }

    // Helper to save user's tasks
    fun saveUserTasks(taskList: List<TaskItem>) {
        val stringSet = taskList.map { "${it.id}|||${it.title}|||${it.isDone}" }.toSet()
        prefs.edit().putStringSet("tasks_list", stringSet).apply()
    }

    var tasks by remember { mutableStateOf(loadUserTasks()) }
    var newTaskText by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf((tasks.maxOfOrNull { it.id } ?: 0) + 1) }

    // Track active pop-up screen (Calendar, Timer, Stopwatch, Account)
    var selectedPopupScreen by remember { mutableStateOf<String?>(null) }
    // Current date state
    val calInstance = remember { java.util.Calendar.getInstance() }
    var selectedDay by remember { mutableIntStateOf(calInstance.get(java.util.Calendar.DAY_OF_MONTH)) }
    var selectedMonth by remember { mutableIntStateOf(calInstance.get(java.util.Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(calInstance.get(java.util.Calendar.YEAR)) }
    var selectedDateText by remember { mutableStateOf("") }

    // Color Palette
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

        // Pop-up Screen Dialog for Calendar, Timer, Stopwatch, Account
        if (selectedPopupScreen != null) {
            AlertDialog(
                onDismissRequest = { selectedPopupScreen = null },
                containerColor = cardBackground,
                title = {
                    Text(
                        text = selectedPopupScreen!!,
                        color = neonPurple,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (selectedPopupScreen) {
                            "Calendar" -> {
                                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                                val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

                                val cal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, selectedYear)
                                    set(java.util.Calendar.MONTH, selectedMonth)
                                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                                }
                                val firstDayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK) - 1
                                val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF4F4F6), shape = RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. Top 3 Boxes: Date, Month, Year
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Date Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("DATE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text("$selectedDay", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Month Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("MONTH", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text(monthNames.getOrElse(selectedMonth) { "" }, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        // Year Box
                                        Box(
                                            modifier = Modifier
                                                .weight(1.1f)
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("YEAR", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                                Text("$selectedYear", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 2. Days Header (S M T W T F S)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        daysOfWeek.forEach { day ->
                                            Text(
                                                text = day,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 3. Calendar Days Grid with Glowing Lavender Selected Circle
                                    val totalSlots = firstDayOfWeek + daysInMonth
                                    val rows = (totalSlots + 6) / 7

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (r in 0 until rows) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                for (c in 0 until 7) {
                                                    val dayNum = r * 7 + c - firstDayOfWeek + 1
                                                    if (dayNum in 1..daysInMonth) {
                                                        val isSelected = (dayNum == selectedDay)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(34.dp)
                                                                .then(
                                                                    if (isSelected) {
                                                                        Modifier
                                                                            .background(
                                                                                color = Color(0xFFE8DEF8), // Light Lavender fill
                                                                                shape = CircleShape
                                                                            )
                                                                             border(
                                                                                width = 2.dp,
                                                                                color = Color(0xFFBB86FC), // Glowing Neon Purple border
                                                                                shape = CircleShape
                                                                            )
                                                                    } else Modifier
                                                                )
                                                                .clickable { selectedDay = dayNum },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "$dayNum",
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) Color(0xFF6750A4) else Color.DarkGray
                                                            )
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.size(34.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "This is the ${selectedPopupScreen!!} Screen",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { selectedPopupScreen = null },
                        colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                    ) {
                        Text("Close", color = darkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}