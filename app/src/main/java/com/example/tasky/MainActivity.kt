package com.example.tasky

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
