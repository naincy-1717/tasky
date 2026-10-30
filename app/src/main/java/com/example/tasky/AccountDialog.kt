package com.example.tasky

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountDialog(
    userEmail: String,
    onDismissRequest: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = {
            Text(
                text = "Account",
                color = neonPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Logged in as:\n$userEmail",
                    color = Color.White,
                    fontSize = 16.sp
                )
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