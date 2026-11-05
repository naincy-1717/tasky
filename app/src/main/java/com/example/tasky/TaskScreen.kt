package com.example.tasky

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(
    userEmail: String = "default_user",
    onLogout: () -> Unit = {}
) {
    // Active tab state ("Tasks", "Calendar", "Timer", "Stopwatch", "Account")
    var selectedTab by remember { mutableStateOf("Tasks") }

    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // MAIN CONTENT AREA
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "Tasks" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Header
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

                        // Single Tasks Area
                        Box(modifier = Modifier.weight(1f)) {
                            TasksTabContent(userEmail = userEmail)
                        }
                    }
                }
                "Calendar" -> CalendarDialog(onDismissRequest = { selectedTab = "Tasks" })
                "Timer" -> TimerDialog(onDismissRequest = { selectedTab = "Tasks" })
                "Stopwatch" -> StopwatchDialog(onDismissRequest = { selectedTab = "Tasks" })
                "Account" -> AccountDialog(userEmail = userEmail, onLogout = onLogout, onDismissRequest = { selectedTab = "Tasks" })
            }
        }

        // BOTTOM NAVIGATION BAR
        Surface(
            color = cardBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val navItems = listOf(
                    "Tasks" to "✓",
                    "Calendar" to "C",
                    "Timer" to "T",
                    "Stopwatch" to "S",
                    "Account" to "A"
                )

                navItems.forEach { (name, initial) ->
                    val isSelected = selectedTab == name

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = name }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    color = if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                color = if (isSelected) darkBackground else lightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            color = if (isSelected) neonPurple else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// TASKS TAB CONTENT: Single Unified List
// ==========================================
@Composable
fun TasksTabContent(userEmail: String) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).taskDao() }
    val coroutineScope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE) }
    val isAccountSynced = remember(userEmail) { prefs.getBoolean("${userEmail}_is_synced", false) }

    val rawTasks by dao.getTasksByUser(userEmail).collectAsState(initial = emptyList<TaskEntity>())
    val tasks = rawTasks.sortedWith(compareBy({ !it.isImportant }, { it.id }))

    var showAddTaskDialog by remember { mutableStateOf(false) }

    // Dialog Input States
    var newTaskInputText by remember { mutableStateOf("") }
    var taskTime by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var isImportant by remember { mutableStateOf(false) }
    var isSyncEnabled by remember { mutableStateOf(isAccountSynced) }

    // Task Conflict Resolution States
    var conflictingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingTaskToInsert by remember { mutableStateOf<TaskEntity?>(null) }

    val neonPurple = Color(0xFFBB86FC)
    val cardBackground = Color(0xFF1E1E1E)
    val lightGray = Color(0xFFE0E0E0)
    val lightThemeBg = Color(0xFFF4F4F6)

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Circular Clock Time Picker
    fun openClockPicker() {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val timeCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                taskTime = timeFormat.format(timeCal.time)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    // Calendar Date Picker
    fun openDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dateCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                taskDate = dateFormat.format(dateCal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = lightThemeBg),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tasks added",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isImportant) Color(0xFFFFF0F0) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = if (task.isImportant) BorderStroke(1.dp, Color(0xFFFF8888)) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = {
                                            coroutineScope.launch { dao.updateTask(task.copy(isDone = !task.isDone)) }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF9C27B0),
                                            checkmarkColor = Color.White
                                        )
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = task.title,
                                                color = if (task.isDone) Color.Gray else Color.Black,
                                                fontSize = 15.sp,
                                                fontWeight = if (task.isImportant) FontWeight.Bold else FontWeight.Normal,
                                                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                                            )

                                            if (task.isImportant) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFF4D4D), shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "IMPORTANT 🔥",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        val details = listOfNotNull(
                                            if (task.time.isNotBlank()) "🕒 ${task.time}" else null,
                                            if (task.date.isNotBlank()) "📅 ${task.date}" else null,
                                            if (task.repeat.isNotBlank()) "🔁 ${task.repeat}" else null
                                        ).joinToString("  |  ")

                                        if (details.isNotBlank()) {
                                            Text(
                                                text = details,
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    IconButton(onClick = {
                                        coroutineScope.launch { dao.deleteTask(task) }
                                    }) {
                                        Text(
                                            text = "✕",
                                            color = Color.Gray,
                                            fontSize = 16.sp,
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

        // CENTER FLOATING PLUS BUTTON
        FloatingActionButton(
            onClick = {
                val cal = Calendar.getInstance()
                newTaskInputText = ""
                taskTime = timeFormat.format(cal.time)
                taskDate = dateFormat.format(cal.time)
                selectedDays = emptySet()
                isImportant = false
                isSyncEnabled = isAccountSynced
                showAddTaskDialog = true
            },
            containerColor = neonPurple,
            contentColor = Color(0xFF121212),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(64.dp)
        ) {
            Text(
                text = "+",
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )
        }
    }

    // ENHANCED ADD TASK DIALOG
    if (showAddTaskDialog) {
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = cardBackground,
            title = {
                Text(
                    text = "Add Task",
                    color = neonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Task Input Field
                    OutlinedTextField(
                        value = newTaskInputText,
                        onValueChange = { newTaskInputText = it },
                        label = { Text("Task title", color = lightGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonPurple,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Time & Date Pickers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Time 🕒", color = lightGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonPurple,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { openClockPicker() }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date 📅", color = lightGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = neonPurple,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { openDatePicker() }
                            )
                        }
                    }

                    // Repeat Days Selection
                    Column {
                        Text("Repeat Days (Optional):", color = lightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            weekDays.forEach { day ->
                                val isSelected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color(0xFF121212) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Category Toggle Slider
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isImportant) Color(0xFF33141A) else Color(0xFF2C2C2C)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isImportant) "Important 🔥" else "Normal Work",
                                color = if (isImportant) Color(0xFFFF6B6B) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF4D4D)
                                )
                            )
                        }
                    }

                    // Cloud Sync Switch Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSyncEnabled) "Cloud Sync: ON 🔄" else "Cloud Sync: OFF 🛑",
                                color = if (isSyncEnabled) Color(0xFF81C784) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = isSyncEnabled,
                                onCheckedChange = {
                                    if (isAccountSynced) isSyncEnabled = it
                                },
                                enabled = isAccountSynced,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF4CAF50)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskInputText.isNotBlank()) {
                            val repeatText = if (selectedDays.isNotEmpty()) selectedDays.joinToString(", ") else "None"
                            val newEntity = TaskEntity(
                                title = newTaskInputText,
                                isDone = false,
                                isTomorrow = false,
                                userEmail = userEmail,
                                time = taskTime,
                                date = taskDate,
                                repeat = repeatText,
                                isImportant = isImportant
                            )

                            val existingMatch = rawTasks.find {
                                it.time == taskTime && it.date == taskDate
                            }

                            if (existingMatch != null && isSyncEnabled) {
                                conflictingTask = existingMatch
                                pendingTaskToInsert = newEntity
                            } else {
                                coroutineScope.launch { dao.insertTask(newEntity) }
                            }
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Save Task", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // CONFLICT RESOLUTION DIALOG
    if (conflictingTask != null && pendingTaskToInsert != null) {
        AlertDialog(
            onDismissRequest = {
                conflictingTask = null
                pendingTaskToInsert = null
            },
            containerColor = cardBackground,
            title = {
                Text(
                    text = "Already a task added.",
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Already a task added at ${conflictingTask?.time}. Do you want to replace it?",
                    color = lightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dao.deleteTask(conflictingTask!!)
                            dao.insertTask(pendingTaskToInsert!!)
                            conflictingTask = null
                            pendingTaskToInsert = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Yes", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        conflictingTask = null
                        pendingTaskToInsert = null
                    }
                ) {
                    Text("No", color = Color.Gray)
                }
            }
        )
    }
}