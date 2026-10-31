package com.example.tasky

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.tasky.ui.theme.TASKYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TASKYTheme {
                val context = applicationContext
                val sharedPreferences = context.getSharedPreferences("TaskyPrefs", MODE_PRIVATE)

                // Check if a user email is already saved in storage
                val savedUser = sharedPreferences.getString("ACTIVE_USER", null)

                // If savedUser has an email, this starts as that email (logged in). If null, they are logged out.
                var loggedInUser by remember { mutableStateOf(savedUser) }

                if (loggedInUser != null) {
                    // Pass the saved email directly to the Room database via TaskScreen
                    TaskScreen(
                        userEmail = loggedInUser!!,
                        onLogout = {
                            sharedPreferences.edit().remove("ACTIVE_USER").apply()
                            loggedInUser = null
                        }
                    )
                } else {
                    LoginScreen(onLoginSuccess = { email ->
                        // 1. Save the email to the phone's storage instantly
                        sharedPreferences.edit().putString("ACTIVE_USER", email).apply()

                        // 2. Update the screen state to navigate to TaskScreen
                        loggedInUser = email
                    })
                }
            }
        }
    }
}
