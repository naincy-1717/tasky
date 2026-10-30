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
fun TimerDialog(
    onDismissRequest: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)

    var timerInputMinutes by remember { mutableIntStateOf(5) }
    var timerInputSeconds by remember { mutableIntStateOf(0) }
    var timerRemainingSeconds by remember { mutableLongStateOf(300L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isTimerActive by remember { mutableStateOf(false) }

    // In-memory countdown loop
    LaunchedEffect(isTimerRunning, timerRemainingSeconds) {
        if (isTimerRunning && timerRemainingSeconds > 0) {
            delay(1000L)
            timerRemainingSeconds--
            if (timerRemainingSeconds == 0L) {
                isTimerRunning = false
            }
        }
    }

    val minutes = timerRemainingSeconds / 60
    val seconds = timerRemainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = {
            Text(
                text = "Timer",
                color = neonPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isTimerActive) {
                    Text("Set Countdown Time", color = Color.Gray, fontSize = 13.sp)
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
                        Text(
                            text = if (timerRemainingSeconds == 0L) "Time's Up!" else formattedTime,
                            color = if (timerRemainingSeconds == 0L) Color(0xFFE57373) else Color.White,
                            fontSize = if (timerRemainingSeconds == 0L) 22.sp else 32.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                            Text("Reset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
            ) {
                Text("Close", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}