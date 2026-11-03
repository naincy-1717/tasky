package com.example.tasky

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun TimerDialog(onDismissRequest: () -> Unit) {
    var timerInputMinutes by remember { mutableIntStateOf(5) }
    var timerInputSeconds by remember { mutableIntStateOf(0) }
    var timerRemainingSeconds by remember { mutableLongStateOf(300L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }

    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val darkBackground = Color(0xFF121212)

    LaunchedEffect(isTimerRunning, timerRemainingSeconds) {
        if (isTimerRunning && timerRemainingSeconds > 0) {
            delay(1000L)
            timerRemainingSeconds--
            if (timerRemainingSeconds == 0L) isTimerRunning = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = { Text("Timer", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (!isTimerActive) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { if (timerInputMinutes < 99) timerInputMinutes++ }) { Text("▲", color = neonPurple) }
                            Text(String.format("%02d", timerInputMinutes), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (timerInputMinutes > 0) timerInputMinutes-- }) { Text("▼", color = neonPurple) }
                        }
                        Text(":", color = Color.White, fontSize = 32.sp, modifier = Modifier.padding(horizontal = 16.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { timerInputSeconds = if (timerInputSeconds < 55) timerInputSeconds + 5 else 0 }) { Text("▲", color = neonPurple) }
                            Text(String.format("%02d", timerInputSeconds), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { timerInputSeconds = if (timerInputSeconds >= 5) timerInputSeconds - 5 else 55 }) { Text("▼", color = neonPurple) }
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
                        colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                    ) {
                        Text("Start Timer", color = darkBackground, fontWeight = FontWeight.Bold)
                    }
                } else {
                    val minutes = timerRemainingSeconds / 60
                    val seconds = timerRemainingSeconds % 60
                    Box(
                        modifier = Modifier.size(160.dp).background(cardBackground, CircleShape).border(3.5.dp, neonPurple, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(String.format("%02d:%02d", minutes, seconds), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = { isTimerRunning = !isTimerRunning }, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                            Text(if (isTimerRunning) "Pause" else "Resume", color = darkBackground, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { isTimerRunning = false; isTimerActive = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                            Text("Reset", color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text("Close", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}