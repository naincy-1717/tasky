package com.example.tasky

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(
    userEmail: String = "default_user",
    onLogout: () -> Unit = {}
) {
    var selectedPopupScreen by remember { mutableStateOf<String?>(null) }

    val darkBackground = Color(0xFF121212)
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
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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

            // Main Content Area: Today's and Tomorrow's Task Blocks
            Box(modifier = Modifier.weight(1f)) {
                TasksTabContent(userEmail = userEmail)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation Row with 4 Circles (C, T, S, A)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                                .size(50.dp)
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

        // Pop-up Screen Dialogs (Calendar, Timer, Stopwatch, Account)
        if (selectedPopupScreen != null) {
            when (selectedPopupScreen) {
                "Calendar" -> CalendarDialog(onDismissRequest = { selectedPopupScreen = null })
                "Timer" -> TimerDialog(onDismissRequest = { selectedPopupScreen = null })
                "Stopwatch" -> StopwatchDialog(onDismissRequest = { selectedPopupScreen = null })
                "Account" -> AccountDialog(
                    userEmail = userEmail,
                    onLogout = onLogout,
                    onDismissRequest = { selectedPopupScreen = null }
                )
            }
        }
    }
}

// ==========================================
// TASKS CONTENT (2 Light Theme Blocks: Today & Tomorrow)
// ==========================================
@Composable
fun TasksTabContent(userEmail: String) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).taskDao() }
    val coroutineScope = rememberCoroutineScope()

    // Fetch live task data from Room DB
    val tasks by dao.getTasksByUser(userEmail).collectAsState(initial = emptyList())

    var addingCategory by remember { mutableStateOf<String?>(null) } // "Today" or "Tomorrow"

    // Dialog Fields
    var newTaskInputText by remember { mutableStateOf("") }
    var taskTime by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var isImportant by remember { mutableStateOf(false) }
    var isSyncEnabled by remember { mutableStateOf(true) }

    val neonPurple = Color(0xFFBB86FC)
    val cardBackground = Color(0xFF1E1E1E)
    val lightGray = Color(0xFFE0E0E0)

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Circular Clock Time Picker Dialog
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

    // Calendar Date Picker Dialog
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

    val todayTasks = tasks.filter { !it.isTomorrow }
    val tomorrowTasks = tasks.filter { it.isTomorrow }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BLOCK 1: TODAY'S TASKS
        TaskBlock(
            title = "Today's Tasks",
            category = "Today",
            tasks = todayTasks,
            onAddClick = {
                val cal = Calendar.getInstance()
                newTaskInputText = ""
                taskTime = timeFormat.format(cal.time)
                taskDate = dateFormat.format(cal.time)
                selectedDays = emptySet()
                isImportant = false
                isSyncEnabled = true
                addingCategory = "Today"
            },
            onToggleDone = { task ->
                coroutineScope.launch { dao.updateTask(task.copy(isDone = !task.isDone)) }
            },
            onDelete = { task ->
                coroutineScope.launch { dao.deleteTask(task) }
            },
            modifier = Modifier.weight(1f)
        )

        // BLOCK 2: TOMORROW'S TASKS
        TaskBlock(
            title = "Tomorrow's Tasks",
            category = "Tomorrow",
            tasks = tomorrowTasks,
            onAddClick = {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                newTaskInputText = ""
                taskTime = "10:00 AM"
                taskDate = dateFormat.format(cal.time)
                selectedDays = emptySet()
                isImportant = false
                isSyncEnabled = true
                addingCategory = "Tomorrow"
            },
            onToggleDone = { task ->
                coroutineScope.launch { dao.updateTask(task.copy(isDone = !task.isDone)) }
            },
            onDelete = { task ->
                coroutineScope.launch { dao.deleteTask(task) }
            },
            modifier = Modifier.weight(1f)
        )
    }

    // ENHANCED ADD TASK DIALOG
    if (addingCategory != null) {
        val isTomorrowBlock = addingCategory == "Tomorrow"
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        AlertDialog(
            onDismissRequest = { addingCategory = null },
            containerColor = cardBackground,
            title = {
                Text(
                    text = "Add Task ($addingCategory)",
                    color = neonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskTime,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Time 🕒", color = lightGray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { openClockPicker() })
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskDate,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Date 📅", color = lightGray) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { openDatePicker() })
                        }
                    }

                    Column {
                        Text("Repeat Days (Optional):", color = lightGray, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                            weekDays.forEach { day ->
                                val isSelected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) neonPurple else Color(0xFF2C2C2C), RoundedCornerShape(6.dp))
                                        .clickable { selectedDays = if (isSelected) selectedDays - day else selectedDays + day }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = day, fontSize = 9.sp, color = if (isSelected) Color(0xFF121212) else Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = if (isImportant) Color(0xFF33141A) else Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isImportant) "Category: Important 🔥" else "Category: Normal",
                                color = if (isImportant) Color(0xFFFF6B6B) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Switch(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF4D4D))
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSyncEnabled) "Cloud Sync: ON 🔄" else "Cloud Sync: OFF 🛑",
                                color = if (isSyncEnabled) Color(0xFF81C784) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Switch(
                                checked = isSyncEnabled,
                                onCheckedChange = { isSyncEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4CAF50))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskInputText.isNotBlank()) {
                            val repeatText = if (selectedDays.isNotEmpty()) selectedDays.joinToString(", ") else addingCategory!!
                            coroutineScope.launch {
                                dao.insertTask(
                                    TaskEntity(
                                        title = newTaskInputText,
                                        isDone = false,
                                        isTomorrow = isTomorrowBlock,
                                        userEmail = userEmail,
                                        time = taskTime,
                                        date = taskDate,
                                        repeat = repeatText,
                                        isImportant = isImportant
                                    )
                                )
                            }
                            addingCategory = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Save Task", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { addingCategory = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun TaskBlock(
    title: String,
    category: String,
    tasks: List<TaskEntity>,
    onAddClick: () -> Unit,
    onToggleDone: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F4F6)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121212)
                )
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFBB86FC), CircleShape)
                ) {
                    Text("+", color = Color(0xFF121212), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks added for $category", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    onCheckedChange = { onToggleDone(task) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9C27B0))
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
                                                    text = "IMPORTANT",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    val details = buildList {
                                        if (task.time.isNotBlank()) add("🕒 ${task.time}")
                                        if (task.date.isNotBlank()) add("📅 ${task.date}")
                                        if (task.repeat.isNotBlank()) add("🔁 ${task.repeat}")
                                    }.joinToString("  |  ")

                                    if (details.isNotBlank()) {
                                        Text(
                                            text = details,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                IconButton(onClick = { onDelete(task) }) {
                                    Text("✕", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}