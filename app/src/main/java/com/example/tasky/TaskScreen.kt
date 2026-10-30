package com.example.tasky

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

data class TaskItem(
    val id: Int,
    val title: String,
    val category: String, // "Today" or "Tomorrow"
    val isDone: Boolean = false,
    val time: String = "",
    val date: String = "",
    val repeat: String = "None",
    val isImportant: Boolean = false
)

@Composable
fun TaskScreen(
    userEmail: String = "default_user",
    onLogout: () -> Unit = {}
) {
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
        // --- TAB CONTENT AREA ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                "Tasks" -> TasksTabContent(userEmail = userEmail)
                "Calendar" -> CalendarTabContent()
                "Timer" -> TimerTabContent()
                "Stopwatch" -> StopwatchTabContent()
                "Account" -> AccountTabContent(userEmail = userEmail, onLogout = onLogout)
            }
        }

        // --- BOTTOM NAVIGATION TAB BAR ---
        Surface(
            color = cardBackground,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 8.dp),
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
                            .padding(horizontal = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    color = if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                    shape = CircleShape
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
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
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. TASKS TAB (Today & Tomorrow Blocks)
// ==========================================
@Composable
fun TasksTabContent(userEmail: String) {
    val context = LocalContext.current
    val prefs = remember(userEmail) {
        context.getSharedPreferences("TaskyTasks_$userEmail", Context.MODE_PRIVATE)
    }

    fun loadUserTasks(): List<TaskItem> {
        val savedSet = prefs.getStringSet("tasks_list", emptySet()) ?: emptySet()
        return savedSet.mapNotNull { raw ->
            val parts = raw.split("|||")
            if (parts.size >= 8) {
                TaskItem(
                    id = parts[0].toIntOrNull() ?: 0,
                    title = parts[1],
                    category = parts[2],
                    isDone = parts[3].toBoolean(),
                    time = parts[4],
                    date = parts[5],
                    repeat = parts[6],
                    isImportant = parts[7].toBoolean()
                )
            } else if (parts.size >= 4) {
                TaskItem(
                    id = parts[0].toIntOrNull() ?: 0,
                    title = parts[1],
                    category = parts[2],
                    isDone = parts[3].toBoolean()
                )
            } else null
        }.sortedBy { it.id }
    }

    fun saveUserTasks(taskList: List<TaskItem>) {
        val stringSet = taskList.map {
            "${it.id}|||${it.title}|||${it.category}|||${it.isDone}|||${it.time}|||${it.date}|||${it.repeat}|||${it.isImportant}"
        }.toSet()
        prefs.edit().putStringSet("tasks_list", stringSet).apply()
    }

    var tasks by remember { mutableStateOf(loadUserTasks()) }
    var nextId by remember { mutableIntStateOf((tasks.maxOfOrNull { it.id } ?: 0) + 1) }

    var addingCategory by remember { mutableStateOf<String?>(null) }

    // Dialog Input States
    var newTaskText by remember { mutableStateOf("") }
    var taskTime by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    var isImportant by remember { mutableStateOf(false) }

    val neonPurple = Color(0xFFBB86FC)
    val cardBackground = Color(0xFF1E1E1E)
    val lightGray = Color(0xFFE0E0E0)

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Function to launch Circular Clock Picker
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

    // Function to launch Calendar Date Picker
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(20.dp))

        // Block 1: Today's Tasks
        TaskBlock(
            title = "Today's Tasks",
            category = "Today",
            tasks = tasks.filter { it.category == "Today" },
            onAddClick = {
                val cal = Calendar.getInstance()
                newTaskText = ""
                taskTime = timeFormat.format(cal.time)
                taskDate = dateFormat.format(cal.time)
                selectedDays = emptySet()
                isImportant = false
                addingCategory = "Today"
            },
            onToggleDone = { task ->
                val updated = tasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                tasks = updated
                saveUserTasks(updated)
            },
            onDelete = { task ->
                val updated = tasks.filter { it.id != task.id }
                tasks = updated
                saveUserTasks(updated)
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Block 2: Tomorrow's Tasks
        TaskBlock(
            title = "Tomorrow's Tasks",
            category = "Tomorrow",
            tasks = tasks.filter { it.category == "Tomorrow" },
            onAddClick = {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                newTaskText = ""
                taskTime = "10:00 AM"
                taskDate = dateFormat.format(cal.time)
                selectedDays = emptySet()
                isImportant = false
                addingCategory = "Tomorrow"
            },
            onToggleDone = { task ->
                val updated = tasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                tasks = updated
                saveUserTasks(updated)
            },
            onDelete = { task ->
                val updated = tasks.filter { it.id != task.id }
                tasks = updated
                saveUserTasks(updated)
            },
            modifier = Modifier.weight(1f)
        )
    }

    // --- ENHANCED ADD TASK DIALOG ---
    if (addingCategory != null) {
        val currentCategory = addingCategory!!
        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        AlertDialog(
            onDismissRequest = { addingCategory = null },
            containerColor = cardBackground,
            title = {
                Text(
                    text = "Add Task ($currentCategory)",
                    color = neonPurple,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Task Title Input
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
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

                    // 2. Clock & Calendar Selectors Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Circular Clock Picker Trigger
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskTime,
                                onValueChange = { },
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

                        // Calendar Picker Trigger
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = taskDate,
                                onValueChange = { },
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

                    // 3. Repeat Option (Day-Wise Multi-select Chips)
                    Column {
                        Text("Repeat Days (Optional):", color = lightGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color(0xFF121212) else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (selectedDays.isEmpty()) {
                            Text(
                                text = "Default: $currentCategory",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // 4. Important / Normal Category Toggle Slider Switch
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isImportant) Color(0xFF33141A) else Color(0xFF2C2C2C)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isImportant) "Category: Important 🔥" else "Category: Normal",
                                    color = if (isImportant) Color(0xFFFF6B6B) else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Toggle slider for task urgency",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            Switch(
                                checked = isImportant,
                                onCheckedChange = { isImportant = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF4D4D),
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color(0xFF444444)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            val repeatText = if (selectedDays.isNotEmpty()) {
                                selectedDays.joinToString(", ")
                            } else {
                                currentCategory
                            }

                            val updated = tasks + TaskItem(
                                id = nextId++,
                                title = newTaskText,
                                category = currentCategory,
                                time = taskTime,
                                date = taskDate,
                                repeat = repeatText,
                                isImportant = isImportant
                            )
                            tasks = updated
                            saveUserTasks(updated)
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
    tasks: List<TaskItem>,
    onAddClick: () -> Unit,
    onToggleDone: (TaskItem) -> Unit,
    onDelete: (TaskItem) -> Unit,
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
                            border = if (task.isImportant) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF8888)) else null,
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
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
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

                                    // Display Details: Time, Date, Repeat
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

// ==========================================
// 2. CALENDAR TAB
// ==========================================
@Composable
fun CalendarTabContent() {
    val todayCal = remember { Calendar.getInstance() }
    val todayDay = remember { todayCal.get(Calendar.DAY_OF_MONTH) }
    val todayMonth = remember { todayCal.get(Calendar.MONTH) }
    val todayYear = remember { todayCal.get(Calendar.YEAR) }

    var selectedDay by remember { mutableIntStateOf(todayDay) }
    var selectedMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedYear by remember { mutableIntStateOf(todayYear) }

    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    val (firstDayOfWeek, daysInMonth) = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        Pair(cal.get(Calendar.DAY_OF_WEEK) - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text("Calendar", color = Color(0xFFBB86FC), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F4F6), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .clickable { selectedDay = todayDay; selectedMonth = todayMonth; selectedYear = todayYear }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DATE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$selectedDay", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .clickable { showMonthPicker = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MONTH ▾", fontSize = 10.sp, color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                        Text(monthNames.getOrElse(selectedMonth) { "" }, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .clickable { showYearPicker = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YEAR ▾", fontSize = 10.sp, color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                        Text("$selectedYear", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val totalSlots = firstDayOfWeek + daysInMonth
            val rows = (totalSlots + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        for (c in 0 until 7) {
                            val dayNum = r * 7 + c - firstDayOfWeek + 1
                            if (dayNum in 1..daysInMonth) {
                                val isToday = (dayNum == todayDay && selectedMonth == todayMonth && selectedYear == todayYear)
                                val isSelected = (dayNum == selectedDay)

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .then(
                                            when {
                                                isSelected -> Modifier.background(Color(0xFFE8DEF8), CircleShape).border(2.dp, Color(0xFFBB86FC), CircleShape)
                                                isToday -> Modifier.border(1.5.dp, Color(0xFF9C27B0), CircleShape)
                                                else -> Modifier
                                            }
                                        )
                                        .clickable { selectedDay = dayNum },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF6750A4) else Color.DarkGray
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMonthPicker) {
        val fullMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        Dialog(onDismissRequest = { showMonthPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.height(350.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Month", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        itemsIndexed(fullMonths) { idx, m ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMonth = idx; showMonthPicker = false }
                                    .padding(12.dp)
                            ) { Text(m, color = Color.White) }
                        }
                    }
                }
            }
        }
    }

    if (showYearPicker) {
        Dialog(onDismissRequest = { showYearPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)), modifier = Modifier.height(350.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Year", color = Color(0xFFBB86FC), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        items((1960..2100).toList()) { yr ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedYear = yr; showYearPicker = false }
                                    .padding(12.dp)
                            ) { Text("$yr", color = Color.White) }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. TIMER TAB
// ==========================================
@Composable
fun TimerTabContent() {
    var timerInputMinutes by remember { mutableIntStateOf(5) }
    var timerInputSeconds by remember { mutableIntStateOf(0) }
    var timerRemainingSeconds by remember { mutableLongStateOf(300L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, timerRemainingSeconds) {
        if (isTimerRunning && timerRemainingSeconds > 0) {
            delay(1000L)
            timerRemainingSeconds--
            if (timerRemainingSeconds == 0L) isTimerRunning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Timer", color = Color(0xFFBB86FC), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        if (!isTimerActive) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { if (timerInputMinutes < 99) timerInputMinutes++ }) { Text("▲", color = Color(0xFFBB86FC)) }
                    Text(String.format("%02d", timerInputMinutes), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { if (timerInputMinutes > 0) timerInputMinutes-- }) { Text("▼", color = Color(0xFFBB86FC)) }
                }
                Text(":", color = Color.White, fontSize = 36.sp, modifier = Modifier.padding(horizontal = 16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { timerInputSeconds = if (timerInputSeconds < 55) timerInputSeconds + 5 else 0 }) { Text("▲", color = Color(0xFFBB86FC)) }
                    Text(String.format("%02d", timerInputSeconds), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { timerInputSeconds = if (timerInputSeconds >= 5) timerInputSeconds - 5 else 55 }) { Text("▼", color = Color(0xFFBB86FC)) }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = {
                    val totalSecs = (timerInputMinutes * 60L) + timerInputSeconds
                    if (totalSecs > 0) {
                        timerRemainingSeconds = totalSecs
                        isTimerActive = true
                        isTimerRunning = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
            ) {
                Text("Start Timer", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
            }
        } else {
            val minutes = timerRemainingSeconds / 60
            val seconds = timerRemainingSeconds % 60
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFF1E1E1E), CircleShape)
                    .border(4.dp, Color(0xFFBB86FC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(String.format("%02d:%02d", minutes, seconds), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { isTimerRunning = !isTimerRunning },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBB86FC))
                ) {
                    Text(if (isTimerRunning) "Pause" else "Resume", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { isTimerRunning = false; isTimerActive = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
                ) {
                    Text("Reset", color = Color.White)
                }
            }
        }
    }
}

// ==========================================
// 4. STOPWATCH TAB
// ==========================================
@Composable
fun StopwatchTabContent() {
    var stopwatchTimeMillis by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isStopwatchRunning) {
        if (isStopwatchRunning) {
            var lastTime = System.currentTimeMillis()
            while (isStopwatchRunning) {
                delay(10L)
                val current = System.currentTimeMillis()
                stopwatchTimeMillis += (current - lastTime)
                lastTime = current
            }
        }
    }

    val minutes = (stopwatchTimeMillis / 1000) / 60
    val seconds = (stopwatchTimeMillis / 1000) % 60
    val hundredths = (stopwatchTimeMillis % 1000) / 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Stopwatch", color = Color(0xFFBB86FC), fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFF1E1E1E), CircleShape)
                .border(4.dp, Color(0xFFBB86FC), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(String.format("%02d:%02d.%02d", minutes, seconds, hundredths), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isStopwatchRunning = !isStopwatchRunning },
                colors = ButtonDefaults.buttonColors(containerColor = if (isStopwatchRunning) Color(0xFFCF6679) else Color(0xFFBB86FC))
            ) {
                Text(if (isStopwatchRunning) "Stop" else "Start", color = Color(0xFF121212), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { isStopwatchRunning = false; stopwatchTimeMillis = 0L },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))
            ) {
                Text("Reset", color = Color.White)
            }
        }
    }
}

// ==========================================
// 5. ACCOUNT TAB
// ==========================================
@Composable
fun AccountTabContent(userEmail: String, onLogout: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE) }

    val savedName = remember { prefs.getString("${userEmail}_name", "New User") ?: "New User" }
    val savedPhone = remember { prefs.getString("${userEmail}_phone", "") ?: "" }
    val savedPassword = remember { prefs.getString(userEmail, "********") ?: "********" }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val userInitial = userEmail.firstOrNull()?.uppercase() ?: "U"
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(neonPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(userInitial, color = Color(0xFF121212), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                prefs.edit().putString("${userEmail}_name", it).apply()
            },
            label = { Text("Name", color = lightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = userEmail,
            onValueChange = { },
            label = { Text("Email", color = lightGray) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.Gray, unfocusedTextColor = Color.Gray),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                prefs.edit().putString("${userEmail}_phone", it).apply()
            },
            label = { Text("Phone", color = lightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = savedPassword,
            onValueChange = { },
            readOnly = true,
            label = { Text("Password", color = lightGray) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Text(if (isPasswordVisible) "👁" else "🙈", fontSize = 18.sp)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Log Out", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}