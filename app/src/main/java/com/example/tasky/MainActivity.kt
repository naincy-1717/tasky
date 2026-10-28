package com.example.tasky

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tasky.ui.theme.TASKYTheme
import java.util.Calendar
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TASKYTheme {
                var isLoggedIn by remember { mutableStateOf(false) }
                var userEmail by remember { mutableStateOf("") }

                if (!isLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = { email ->
                            userEmail = email
                            isLoggedIn = true
                        }
                    )
                } else {
                    TaskScreen(userEmail = userEmail)
                }
            }
        }
    }
}



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

    // Active pop-up screen (Calendar, Timer, Stopwatch, Account)
    var selectedPopupScreen by remember { mutableStateOf<String?>(null) }

    // Today's actual date markers (constant)
    val todayCal = remember { Calendar.getInstance() }
    val todayDay = remember { todayCal.get(Calendar.DAY_OF_MONTH) }
    val todayMonth = remember { todayCal.get(Calendar.MONTH) }
    val todayYear = remember { todayCal.get(Calendar.YEAR) }

    // Currently active selection cursor
    var selectedDay by remember { mutableIntStateOf(todayDay) }
    var selectedMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedYear by remember { mutableIntStateOf(todayYear) }

    // Picker Dialog States
    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // Reset calendar selection cursor back to Today whenever Calendar popup opens
    LaunchedEffect(selectedPopupScreen) {
        if (selectedPopupScreen == "Calendar") {
            val freshCal = Calendar.getInstance()
            selectedDay = freshCal.get(Calendar.DAY_OF_MONTH)
            selectedMonth = freshCal.get(Calendar.MONTH)
            selectedYear = freshCal.get(Calendar.YEAR)
        }
    }

    // Stopwatch State Variables
    var stopwatchTimeMillis by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }

    // Stopwatch Loop
    LaunchedEffect(isStopwatchRunning) {
        if (isStopwatchRunning) {
            var lastTime = System.currentTimeMillis()
            while (isStopwatchRunning) {
                delay(10L)
                val currentTime = System.currentTimeMillis()
                stopwatchTimeMillis += (currentTime - lastTime)
                lastTime = currentTime
            }
        }
    }

    // Timer State Variables
    var timerInputMinutes by remember { mutableIntStateOf(5) }
    var timerInputSeconds by remember { mutableIntStateOf(0) }
    var timerRemainingSeconds by remember { mutableLongStateOf(300L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }

    // Timer Loop
    LaunchedEffect(isTimerRunning, timerRemainingSeconds) {
        if (isTimerRunning && timerRemainingSeconds > 0) {
            delay(1000L)
            timerRemainingSeconds--
            if (timerRemainingSeconds == 0L) {
                isTimerRunning = false
            }
        }
    }

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

        // Pop-up Screen Dialog
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

                                val (firstDayOfWeek, daysInMonth) = remember(selectedMonth, selectedYear) {
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                    }
                                    val first = cal.get(Calendar.DAY_OF_WEEK) - 1
                                    val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                    Pair(first, max)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF4F4F6), shape = RoundedCornerShape(16.dp))
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. Top 3 Boxes (DATE, MONTH, YEAR)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
                                                .clickable {
                                                    selectedDay = todayDay
                                                    selectedMonth = todayMonth
                                                    selectedYear = todayYear
                                                }
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
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
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
                                                .background(Color.White, shape = RoundedCornerShape(10.dp))
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

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // 2. Day Header Row (S M T W T F S)
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

                                    // 3. Days Grid
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
                                                        val isToday = (dayNum == todayDay && selectedMonth == todayMonth && selectedYear == todayYear)
                                                        val isSelected = (dayNum == selectedDay)

                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .then(
                                                                    when {
                                                                        isSelected -> Modifier
                                                                            .background(Color(0xFFE8DEF8), shape = CircleShape)
                                                                            .border(2.dp, Color(0xFFBB86FC), shape = CircleShape)
                                                                        isToday -> Modifier.border(1.5.dp, Color(0xFF9C27B0), shape = CircleShape)
                                                                        else -> Modifier
                                                                    }
                                                                )
                                                                .clickable { selectedDay = dayNum },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text(
                                                                    text = "$dayNum",
                                                                    fontSize = 13.sp,
                                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                                    color = when {
                                                                        isSelected -> Color(0xFF6750A4)
                                                                        isToday -> Color(0xFF9C27B0)
                                                                        else -> Color.DarkGray
                                                                    }
                                                                )
                                                                if (isToday) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(4.dp)
                                                                            .background(Color(0xFF9C27B0), shape = CircleShape)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Spacer(modifier = Modifier.size(36.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "Timer" -> {
                                val minutes = timerRemainingSeconds / 60
                                val seconds = timerRemainingSeconds % 60
                                val formattedTime = String.format("%02d:%02d", minutes, seconds)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!isTimerActive) {
                                        Text(
                                            text = "Set Countdown Time",
                                            color = Color.Gray,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                IconButton(onClick = { if (timerInputMinutes < 99) timerInputMinutes++ }) {
                                                    Text("▲", color = neonPurple, fontSize = 18.sp)
                                                }
                                                Text(
                                                    text = String.format("%02d", timerInputMinutes),
                                                    color = Color.White,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(onClick = { if (timerInputMinutes > 0) timerInputMinutes-- }) {
                                                    Text("▼", color = neonPurple, fontSize = 18.sp)
                                                }
                                                Text("MIN", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Text(
                                                text = ":",
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                IconButton(onClick = {
                                                    timerInputSeconds = if (timerInputSeconds < 55) timerInputSeconds + 5 else 0
                                                }) {
                                                    Text("▲", color = neonPurple, fontSize = 18.sp)
                                                }
                                                Text(
                                                    text = String.format("%02d", timerInputSeconds),
                                                    color = Color.White,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                IconButton(onClick = {
                                                    timerInputSeconds = if (timerInputSeconds >= 5) timerInputSeconds - 5 else 55
                                                }) {
                                                    Text("▼", color = neonPurple, fontSize = 18.sp)
                                                }
                                                Text("SEC", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                val totalSecs = (timerInputMinutes * 60L) + timerInputSeconds
                                                if (totalSecs > 0) {
                                                    timerRemainingSeconds = totalSecs
                                                    isTimerActive = true
                                                    isTimerRunning = true
                                                }
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(46.dp)
                                        ) {
                                            Text("Start Timer", color = darkBackground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }

                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(170.dp)
                                                .background(cardBackground, shape = CircleShape)
                                                .border(
                                                    3.5.dp,
                                                    if (timerRemainingSeconds == 0L) Color(0xFFE57373) else neonPurple,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = if (timerRemainingSeconds == 0L) "Time's Up!" else formattedTime,
                                                    color = if (timerRemainingSeconds == 0L) Color(0xFFE57373) else Color.White,
                                                    fontSize = if (timerRemainingSeconds == 0L) 22.sp else 32.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(24.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(0.9f),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    if (timerRemainingSeconds > 0) {
                                                        isTimerRunning = !isTimerRunning
                                                    }
                                                },
                                                enabled = timerRemainingSeconds > 0,
                                                shape = RoundedCornerShape(50),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isTimerRunning) Color(0xFFE57373) else neonPurple
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text(
                                                    text = if (isTimerRunning) "Pause" else "Resume",
                                                    color = darkBackground,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    isTimerRunning = false
                                                    isTimerActive = false
                                                    timerRemainingSeconds = (timerInputMinutes * 60L) + timerInputSeconds
                                                },
                                                shape = RoundedCornerShape(50),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text(
                                                    text = "Reset",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "Stopwatch" -> {
                                val minutes = (stopwatchTimeMillis / 1000) / 60
                                val seconds = (stopwatchTimeMillis / 1000) % 60
                                val hundredths = (stopwatchTimeMillis % 1000) / 10
                                val formattedTime = String.format("%02d:%02d.%02d", minutes, seconds, hundredths)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(170.dp)
                                            .background(cardBackground, shape = CircleShape)
                                            .border(3.5.dp, neonPurple, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = formattedTime,
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(0.9f),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Button(
                                            onClick = { isStopwatchRunning = !isStopwatchRunning },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isStopwatchRunning) Color(0xFFE57373) else neonPurple
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Text(
                                                text = if (isStopwatchRunning) "Stop" else "Start",
                                                color = darkBackground,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                isStopwatchRunning = false
                                                stopwatchTimeMillis = 0L
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Text(
                                                text = "Reset",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
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

        // --- MONTH PICKER DIALOG ---
        if (showMonthPicker) {
            val fullMonths = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            Dialog(onDismissRequest = { showMonthPicker = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(350.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select Month",
                            color = neonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(fullMonths) { index, monthName ->
                                val isSelected = (index == selectedMonth)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedMonth = index
                                            val maxDays = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, selectedYear)
                                                set(Calendar.MONTH, selectedMonth)
                                            }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                            if (selectedDay > maxDays) selectedDay = maxDays
                                            showMonthPicker = false
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = monthName,
                                        color = if (isSelected) darkBackground else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- YEAR PICKER DIALOG (1960 to 2100) ---
        if (showYearPicker) {
            val yearsList = (1960..2100).toList()
            val initialIndex = (selectedYear - 1960).coerceIn(0, yearsList.size - 1)
            val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

            Dialog(onDismissRequest = { showYearPicker = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBackground),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(380.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select Year (1960 - 2100)",
                            color = neonPurple,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(yearsList) { year ->
                                val isSelected = (year == selectedYear)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedYear = year
                                            val maxDays = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, selectedYear)
                                                set(Calendar.MONTH, selectedMonth)
                                            }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                            if (selectedDay > maxDays) selectedDay = maxDays
                                            showYearPicker = false
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$year",
                                        color = if (isSelected) darkBackground else Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}