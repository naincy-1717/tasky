package com.example.tasky

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountDialog(
    userEmail: String,
    onLogout: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE) }

    val savedName = remember { prefs.getString("${userEmail}_name", "New User") ?: "New User" }
    val savedPhone = remember { prefs.getString("${userEmail}_phone", "") ?: "" }
    val savedPassword = remember { prefs.getString(userEmail, "********") ?: "********" }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val userInitial = userEmail.firstOrNull()?.uppercase() ?: "U"
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val darkBackground = Color(0xFF121212)
    val lightGray = Color(0xFFE0E0E0)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = { Text("Account Profile", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(54.dp).background(neonPurple, CircleShape), contentAlignment = Alignment.Center) {
                    Text(userInitial, color = darkBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; prefs.edit().putString("${userEmail}_name", it).apply() },
                    label = { Text("Name", color = lightGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email", color = lightGray) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.Gray, unfocusedTextColor = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; prefs.edit().putString("${userEmail}_phone", it).apply() },
                    label = { Text("Phone", color = lightGray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = savedPassword,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Password", color = lightGray) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(if (isPasswordVisible) "👁" else "🙈", fontSize = 16.sp)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonPurple, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text("Done", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onLogout) {
                Text("Log Out", color = Color(0xFFCF6679), fontWeight = FontWeight.Bold)
            }
        }
    )
}