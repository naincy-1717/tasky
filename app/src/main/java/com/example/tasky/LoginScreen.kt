package com.example.tasky

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit = {}) {
    var isSignUpMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }

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
            text = if (isSignUpMode) "Create an Account" else "Welcome Back",
            color = neonPurple,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Name Field (Only visible during Sign Up)
        if (isSignUpMode) {
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
        }

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

        // Action Button
        Button(
            onClick = {
                val cleanEmail = email.trim().lowercase()

                // Basic validation
                val isValidEmail = cleanEmail.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
                if (!isValidEmail) {
                    errorMessage = "Invalid email format! (e.g., name@gmail.com)"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                if (isSignUpMode) {
                    // Firebase Cloud Sign Up
                    auth.createUserWithEmailAndPassword(cleanEmail, password)
                        .addOnSuccessListener { result ->
                            isLoading = false
                            val user = result.user
                            if (name.isNotBlank() && user != null) {
                                val profileUpdate = userProfileChangeRequest {
                                    displayName = name.trim()
                                }
                                user.updateProfile(profileUpdate)
                            }
                            onLoginSuccess(cleanEmail)
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Sign up failed"
                        }
                } else {
                    // Firebase Cloud Log In
                    auth.signInWithEmailAndPassword(cleanEmail, password)
                        .addOnSuccessListener {
                            isLoading = false
                            onLoginSuccess(cleanEmail)
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = e.localizedMessage ?: "Login failed"
                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = darkBackground,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = if (isSignUpMode) "Sign Up" else "Log In",
                    color = darkBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle text at the bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                color = lightGray,
                fontSize = 14.sp
            )
            Text(
                text = if (isSignUpMode) "Log In" else "Sign Up",
                color = neonPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                    isSignUpMode = !isSignUpMode
                    name = ""
                    email = ""
                    password = ""
                    isPasswordVisible = false
                    errorMessage = null
                }
            )
        }
    }
}