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
    onLogout: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TaskyAuthPrefs", Context.MODE_PRIVATE) }

    val savedName = remember { prefs.getString("${userEmail}_name", "New User") ?: "New User" }
    val savedPhone = remember { prefs.getString("${userEmail}_phone", "") ?: "" }
    val savedPassword = remember { prefs.getString(userEmail, "********") ?: "********" }

    var name by remember { mutableStateOf(savedName) }
    var phone by remember { mutableStateOf(savedPhone) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Sync States
    var isSynced by remember { mutableStateOf(prefs.getBoolean("${userEmail}_is_synced", false)) }
    var syncPartnerCode by remember { mutableStateOf(prefs.getString("${userEmail}_sync_code", "") ?: "") }
    var syncPartnerEmail by remember { mutableStateOf(prefs.getString("${userEmail}_sync_partner", "") ?: "") }

    var showSyncChoiceDialog by remember { mutableStateOf(false) }
    var showGeneratedCodeDialog by remember { mutableStateOf(false) }
    var showEnterCodeDialog by remember { mutableStateOf(false) }

    var generatedCode by remember { mutableStateOf("") }
    var inputCode by remember { mutableStateOf("") }
    var syncErrorMessage by remember { mutableStateOf<String?>(null) }

    val userInitial = userEmail.firstOrNull()?.uppercase() ?: "U"
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val darkBackground = Color(0xFF121212)
    val lightGray = Color(0xFFE0E0E0)
    val errorRed = Color(0xFFCF6679)

    // Function to generate a unique 4-digit system code and register it in global memory
    fun generate4DigitCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        do {
            code = (1..4).map { chars.random() }.joinToString("")
        } while (prefs.contains("global_sync_code_$code"))

        // Register code in system shared memory mapped to this creator user
        prefs.edit().putString("global_sync_code_$code", userEmail).apply()
        return code
    }

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

                Spacer(modifier = Modifier.height(14.dp))

                // Want to Sync Section
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSynced) Color(0xFF1B3A27) else Color(0xFF2C2C2C)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSynced) {
                            Text(
                                text = "Accounts Synced 🔄 ($syncPartnerCode)",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (syncPartnerEmail.isNotBlank()) {
                                Text(
                                    text = "Synced with: $syncPartnerEmail",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(onClick = {
                                // Unlink both accounts
                                if (syncPartnerEmail.isNotBlank()) {
                                    prefs.edit().putBoolean("${syncPartnerEmail}_is_synced", false).apply()
                                    prefs.edit().remove("${syncPartnerEmail}_sync_code").apply()
                                    prefs.edit().remove("${syncPartnerEmail}_sync_partner").apply()
                                }
                                if (syncPartnerCode.isNotBlank()) {
                                    prefs.edit().remove("global_sync_code_$syncPartnerCode").apply()
                                }
                                isSynced = false
                                syncPartnerCode = ""
                                syncPartnerEmail = ""
                                prefs.edit().putBoolean("${userEmail}_is_synced", false).apply()
                                prefs.edit().remove("${userEmail}_sync_code").apply()
                                prefs.edit().remove("${userEmail}_sync_partner").apply()
                            }) {
                                Text("Unlink Account", color = errorRed, fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = { showSyncChoiceDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = neonPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Want to Sync? 🔗", color = darkBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text("Done", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onLogout) {
                Text("Log Out", color = errorRed, fontWeight = FontWeight.Bold)
            }
        }
    )

    // Option Dialog: Send Sync Code OR Received a Code?
    if (showSyncChoiceDialog) {
        AlertDialog(
            onDismissRequest = { showSyncChoiceDialog = false },
            containerColor = cardBackground,
            title = { Text("Account Synchronization", color = neonPurple, fontWeight = FontWeight.Bold) },
            text = { Text("Choose how you want to connect with your partner:", color = lightGray) },
            confirmButton = {
                Button(
                    onClick = {
                        generatedCode = generate4DigitCode()
                        showSyncChoiceDialog = false
                        showGeneratedCodeDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Send Sync Code?", color = darkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        inputCode = ""
                        syncErrorMessage = null
                        showSyncChoiceDialog = false
                        showEnterCodeDialog = true
                    }
                ) {
                    Text("Received a Code?", color = neonPurple)
                }
            }
        )
    }

    // Generated Code Dialog
    if (showGeneratedCodeDialog) {
        AlertDialog(
            onDismissRequest = { showGeneratedCodeDialog = false },
            containerColor = cardBackground,
            title = { Text("Your Sync Code", color = neonPurple, fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Share this 4-digit code with your partner:", color = lightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Color(0xFF2C2C2C),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = generatedCode,
                            color = neonPurple,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                        )
                    }
                    Text("Waiting for partner to enter code...", color = Color.Gray, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGeneratedCodeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Done / Share Code", color = darkBackground, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Enter Code Dialog (Strict verification against system-generated codes)
    if (showEnterCodeDialog) {
        AlertDialog(
            onDismissRequest = { showEnterCodeDialog = false },
            containerColor = cardBackground,
            title = { Text("Enter Received Code", color = neonPurple, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the 4-digit code generated by your partner:", color = lightGray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = {
                            if (it.length <= 4) inputCode = it.uppercase()
                            syncErrorMessage = null
                        },
                        label = { Text("4-Digit Code", color = lightGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonPurple,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (syncErrorMessage != null) {
                        Text(syncErrorMessage!!, color = errorRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanCode = inputCode.trim().uppercase()
                        if (cleanCode.length != 4) {
                            syncErrorMessage = "Code must be exactly 4 characters!"
                            return@Button
                        }

                        // Real verification: Lookup code in system database/shared preferences
                        val creatorEmail = prefs.getString("global_sync_code_$cleanCode", null)

                        if (creatorEmail == null) {
                            syncErrorMessage = "Invalid code! No account generated this code."
                        } else if (creatorEmail.equals(userEmail, ignoreCase = true)) {
                            syncErrorMessage = "You cannot sync with your own code!"
                        } else {
                            // REAL SYNC SUCCESS: Link both accounts mutually
                            isSynced = true
                            syncPartnerCode = cleanCode
                            syncPartnerEmail = creatorEmail

                            // Save sync state for current user
                            prefs.edit().putBoolean("${userEmail}_is_synced", true).apply()
                            prefs.edit().putString("${userEmail}_sync_code", cleanCode).apply()
                            prefs.edit().putString("${userEmail}_sync_partner", creatorEmail).apply()

                            // Save sync state for partner user
                            prefs.edit().putBoolean("${creatorEmail}_is_synced", true).apply()
                            prefs.edit().putString("${creatorEmail}_sync_code", cleanCode).apply()
                            prefs.edit().putString("${creatorEmail}_sync_partner", userEmail).apply()

                            showEnterCodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
                ) {
                    Text("Sync Account", color = darkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnterCodeDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}