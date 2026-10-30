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
import androidx.compose.ui.window.Dialog

@Composable
fun AccountDialog(
    userEmail: String,
    onDismissRequest: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE)
    }

    // Load saved Name, Phone, and Password for this user
    val savedName = remember { sharedPreferences.getString("${userEmail}_name", "New User") ?: "New User" }
    val savedPhone = remember { sharedPreferences.getString("${userEmail}_phone", "") ?: "" }
    val savedPassword = remember { sharedPreferences.getString(userEmail, "********") ?: "********" }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)
    val errorRed = Color(0xFFCF6679)

    val userInitial = userEmail.firstOrNull()?.uppercase() ?: "U"

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Top Left Circle with User Initial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(neonPurple, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            color = darkBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name Field (Loaded from Login & Saved on Change)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        sharedPreferences.edit().putString("${userEmail}_name", it).apply()
                    },
                    label = { Text("Name", color = lightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email Field (Read-only)
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = { },
                    label = { Text("Email", color = lightGray) },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.Gray,
                        unfocusedTextColor = Color.Gray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phone Field (Saves to SharedPreferences on entry)
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        sharedPreferences.edit().putString("${userEmail}_phone", it).apply()
                    },
                    label = { Text("Phone", color = lightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Field with See (Eye) Toggle Button
                OutlinedTextField(
                    value = savedPassword,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Password", color = lightGray) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Text(
                                text = if (isPasswordVisible) "👁" else "🙈",
                                fontSize = 18.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonPurple,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Log Out Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = errorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Log Out",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = lightGray)
                }
            }
        }
    }
}