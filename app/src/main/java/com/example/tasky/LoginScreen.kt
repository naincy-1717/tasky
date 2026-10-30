package com.example.tasky

import android.content.Context
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit = {}) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Persistent storage on device
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE)
    }

    // Color Palette
    val darkBackground = Color(0xFF121212)
    val neonPurple = Color(0xFFBB86FC)
    val lightGray = Color(0xFFE0E0E0)
    val errorRed = Color(0xFFCF6679)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to TASKY",
            color = neonPurple,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
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

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email", color = lightGray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonPurple,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password", color = lightGray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonPurple,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        // Error Display
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = errorRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Enter Button
        Button(
            onClick = {
                val cleanEmail = email.trim().lowercase()

                // 1. Email Format Check
                val isValidEmail = cleanEmail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()

                if (!isValidEmail) {
                    errorMessage = "Invalid email format! (e.g., name@gmail.com)"
                    return@Button
                }

                if (password.isBlank()) {
                    errorMessage = "Password cannot be empty"
                    return@Button
                }

                // Save user name if provided
                if (name.isNotBlank()) {
                    sharedPreferences.edit().putString("${cleanEmail}_name", name.trim()).apply()
                }

                // 2. Password Check & Registration
                if (sharedPreferences.contains(cleanEmail)) {
                    val storedPassword = sharedPreferences.getString(cleanEmail, null)
                    if (storedPassword == password) {
                        onLoginSuccess(cleanEmail)
                    } else {
                        errorMessage = "Incorrect password for this email!"
                    }
                } else {
                    // Register email + password
                    sharedPreferences.edit().putString(cleanEmail, password).apply()
                    onLoginSuccess(cleanEmail)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Enter",
                color = darkBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}