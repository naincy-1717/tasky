package com.example.tasky

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
fun CalendarDialog(
    onDismissRequest: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val lightBlockBackground = Color(0xFFF4F4F6)
    val neonPurple = Color(0xFFBB86FC)
    val darkPurple = Color(0xFF6750A4)

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

    val (firstDayOfWeek, daysInMonth) = remember(selectedMonth, selectedYear) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val first = cal.get(Calendar.DAY_OF_WEEK) - 1
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        Pair(first, max)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = cardBackground,
        title = {
            Text(
                text = "Calendar",
                color = neonPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lightBlockBackground, shape = RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .clickable {
                                selectedDay = todayDay
                                selectedMonth = todayMonth
                                selectedYear = todayYear
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DATE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            Text("$selectedDay", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .clickable { showMonthPicker = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MONTH ▾", fontSize = 10.sp, color = darkPurple, fontWeight = FontWeight.Bold)
                            Text(monthNames.getOrElse(selectedMonth) { "" }, fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .background(Color.White, shape = RoundedCornerShape(10.dp))
                            .clickable { showYearPicker = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YEAR ▾", fontSize = 10.sp, color = darkPurple, fontWeight = FontWeight.Bold)
                            Text("$selectedYear", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    daysOfWeek.forEach { day ->
                        Text(text = day, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val totalSlots = firstDayOfWeek + daysInMonth
                val rows = (totalSlots + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
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
                                                    isSelected -> Modifier
                                                        .background(Color(0xFFE8DEF8), shape = CircleShape)
                                                        .border(2.dp, neonPurple, shape = CircleShape)
                                                    isToday -> Modifier.border(1.5.dp, Color(0xFF9C27B0), shape = CircleShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clickable { selectedDay = dayNum },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$dayNum",
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSelected -> darkPurple
                                                    isToday -> Color(0xFF9C27B0)
                                                    else -> Color.DarkGray
                                                }
                                            )
                                            if (isToday) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(Color(0xFF9C27B0), shape = CircleShape)
                                                )
                                            }
                                        }
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
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = neonPurple)
            ) {
                Text("Close", color = darkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            selectedDay = selectedDay,
            onMonthSelected = { month, day ->
                selectedMonth = month
                selectedDay = day
            },
            onDismiss = { showMonthPicker = false }
        )
    }

    if (showYearPicker) {
        YearPickerDialog(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            selectedDay = selectedDay,
            onYearSelected = { year, day ->
                selectedYear = year
                selectedDay = day
            },
            onDismiss = { showYearPicker = false }
        )
    }
}

@Composable
private fun MonthPickerDialog(
    selectedMonth: Int,
    selectedYear: Int,
    selectedDay: Int,
    onMonthSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val fullMonths = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(350.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Month", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(fullMonths) { index, monthName ->
                        val isSelected = (index == selectedMonth)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val maxDays = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, index)
                                    }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                    val newDay = if (selectedDay > maxDays) maxDays else selectedDay
                                    onMonthSelected(index, newDay)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = monthName,
                                color = if (isSelected) darkBackground else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearPickerDialog(
    selectedMonth: Int,
    selectedYear: Int,
    selectedDay: Int,
    onYearSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val darkBackground = Color(0xFF121212)
    val cardBackground = Color(0xFF1E1E1E)
    val neonPurple = Color(0xFFBB86FC)
    val yearsList = (1960..2100).toList()
    val initialIndex = (selectedYear - 1960).coerceIn(0, yearsList.size - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(380.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Year (1960 - 2100)", color = neonPurple, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(yearsList) { year ->
                        val isSelected = (year == selectedYear)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) neonPurple else Color(0xFF2C2C2C),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val maxDays = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, selectedMonth)
                                    }.getActualMaximum(Calendar.DAY_OF_MONTH)
                                    val newDay = if (selectedDay > maxDays) maxDays else selectedDay
                                    onYearSelected(year, newDay)
                                    onDismiss()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$year",
                                color = if (isSelected) darkBackground else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}