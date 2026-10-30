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
fun StopwatchDialog(
    onDismissRequest: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)

    var stopwatchTimeMillis by remember { mutableLongStateOf(0L) }
    var isStopwatchRunning by remember { mutableStateOf(false) }

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

    val minutes = (stopwatchTimeMillis / 1000) / 60
    val seconds = (stopwatchTimeMillis / 1000) % 60
    val hundredths = (stopwatchTimeMillis % 1000) / 10
    val formattedTime = String.format("%02d:%02d.%02d", minutes, seconds, hundredths)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = {
            Text(
                text = "Stopwatch",
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
                        Text("Reset", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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