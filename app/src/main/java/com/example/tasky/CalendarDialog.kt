package com.example.tasky

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import java.util.Calendar

@Composable
fun CalendarDialog(onDismissRequest: () -> Unit) {
    val todayCal = remember { Calendar.getInstance() }
    val todayDay = remember { todayCal.get(Calendar.DAY_OF_MONTH) }
    val todayMonth = remember { todayCal.get(Calendar.MONTH) }
    val todayYear = remember { todayCal.get(Calendar.YEAR) }

    var selectedDay by remember { mutableIntStateOf(todayDay) }
    var selectedMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedYear by remember { mutableIntStateOf(todayYear) }

    var showMonthPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val darkBackground = Color(0xFF121212)

    val (firstDayOfWeek, daysInMonth) = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        Pair(cal.get(Calendar.DAY_OF_WEEK) - 1, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = { Text("Calendar", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF4F4F6), shape = RoundedCornerShape(16.dp)).padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(10.dp)).clickable { selectedDay = todayDay; selectedMonth = todayMonth; selectedYear = todayYear }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DATE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("$selectedDay", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1.2f).background(Color.White, RoundedCornerShape(10.dp)).clickable { showMonthPicker = true }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MONTH ▾", fontSize = 10.sp, color = neonPurple, fontWeight = FontWeight.Bold)
                            Text(monthNames.getOrElse(selectedMonth) { "" }, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1.1f).background(Color.White, RoundedCornerShape(10.dp)).clickable { showYearPicker = true }.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YEAR ▾", fontSize = 10.sp, color = neonPurple, fontWeight = FontWeight.Bold)
                            Text("$selectedYear", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    daysOfWeek.forEach { day -> Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalSlots = firstDayOfWeek + daysInMonth
                val rows = (totalSlots + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (c in 0 until 7) {
                                val dayNum = r * 7 + c - firstDayOfWeek + 1
                                if (dayNum in 1..daysInMonth) {
                                    val isToday = (dayNum == todayDay && selectedMonth == todayMonth && selectedYear == todayYear)
                                    val isSelected = (dayNum == selectedDay)

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .then(
                                                when {
                                                    isSelected -> Modifier.background(Color(0xFFE8DEF8), CircleShape).border(2.dp, neonPurple, CircleShape)
                                                    isToday -> Modifier.border(1.5.dp, Color(0xFF9C27B0), CircleShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color(0xFF6750A4)
                                                isToday -> Color(0xFF9C27B0)
                                                else -> Color.DarkGray
                                            }
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest, colors = ButtonDefaults.buttonColors(containerColor = neonPurple)) {
                Text("Close", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showMonthPicker) {
        val fullMonths = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        Dialog(onDismissRequest = { showMonthPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBackground), modifier = Modifier.height(350.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Month", color = neonPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        itemsIndexed(fullMonths) { idx, m ->
                            Box(modifier = Modifier.fillMaxWidth().clickable { selectedMonth = idx; showMonthPicker = false }.padding(12.dp)) {
                                Text(m, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showYearPicker) {
        Dialog(onDismissRequest = { showYearPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = cardBackground), modifier = Modifier.height(350.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Year", color = neonPurple, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        items((1960..2100).toList()) { yr ->
                            Box(modifier = Modifier.fillMaxWidth().clickable { selectedYear = yr; showYearPicker = false }.padding(12.dp)) {
                                Text("$yr", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}