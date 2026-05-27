package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Session
import com.example.data.database.SessionLog
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: FitnessViewModel
) {
    val sessions by viewModel.sessions.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedWorkDate by remember { mutableStateOf<Calendar>(Calendar.getInstance()) }

    val formattedSelectedDateString = remember(selectedWorkDate) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
        format.format(selectedWorkDate.time)
    }

    // Sessions logged on the selected calendar date
    val selectedDaySessions = remember(sessions, formattedSelectedDateString) {
        sessions.filter {
            val sDateStr = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(Date(it.dateMillis))
            sDateStr == formattedSelectedDateString
        }
    }

    // Days with saved workouts in the visible month
    val daysWithWorkoutsInMonthSet = remember(sessions, calendarMonth) {
        val set = mutableSetOf<Int>()
        val formatMonth = SimpleDateFormat("yyyy-MM", Locale("es", "ES"))
        val visibleMonthStr = formatMonth.format(calendarMonth.time)

        sessions.forEach { s ->
            val sMonthStr = SimpleDateFormat("yyyy-MM", Locale("es", "ES")).format(Date(s.dateMillis))
            if (sMonthStr == visibleMonthStr) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = s.dateMillis
                set.add(cal.get(Calendar.DAY_OF_MONTH))
            }
        }
        set
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Title
        item {
            Text(
                text = "Historial de Entrenamiento",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Calendar header & Month navigator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val formatMonthHeader = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
                val monthTitle = formatMonthHeader.format(calendarMonth.time).replaceFirstChar { it.uppercase() }

                Text(
                    text = monthTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val nextMock = calendarMonth.clone() as Calendar
                            nextMock.add(Calendar.MONTH, -1)
                            calendarMonth = nextMock
                        },
                        modifier = Modifier.background(com.example.ui.theme.AmoledSurface, CircleShape).border(1.dp, com.example.ui.theme.PremiumGradientBorder, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val nextMock = calendarMonth.clone() as Calendar
                            nextMock.add(Calendar.MONTH, 1)
                            calendarMonth = nextMock
                        },
                        modifier = Modifier.background(com.example.ui.theme.AmoledSurface, CircleShape).border(1.dp, com.example.ui.theme.PremiumGradientBorder, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = Color.White)
                    }
                }
            }
        }

        // Calendar Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(16.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Day initials
                Row(modifier = Modifier.fillMaxWidth()) {
                    val initials = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
                    initials.forEach { d ->
                        Text(
                            text = d,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecundario
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grid mapping dates
                val datesGrid = remember(calendarMonth) { getCalendarMonthGridDates(calendarMonth) }
                datesGrid.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        week.forEach { calVal ->
                            if (calVal == null) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            } else {
                                val dayNum = calVal.get(Calendar.DAY_OF_MONTH)
                                val hasWorkout = daysWithWorkoutsInMonthSet.contains(dayNum)

                                val calCompareFormat = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
                                val isSelected = calCompareFormat.format(calVal.time) == formattedSelectedDateString

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(4.dp)
                                        .background(
                                            if (isSelected) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            width = if (hasWorkout && !isSelected) 1.dp else 0.dp,
                                            color = if (hasWorkout) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedWorkDate = calVal },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                        if (hasWorkout && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .background(Color.White, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Details of selected Day below
        item {
            val displayTitleDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(selectedWorkDate.time).replaceFirstChar { it.uppercase() }
            Text(
                text = "Sesiones de: $displayTitleDate",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecundario,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (selectedDaySessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay entrenamientos registrados para este día.",
                        color = TextSecundario,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(selectedDaySessions) { session ->
                val sessionLogs = logs.filter { it.sessionId == session.id }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = "Active Workout",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = session.routineName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(6.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${session.durationMinutes} min",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Group logs by exercise for neat nested representation
                    val groupedLogs = sessionLogs.groupBy { it.exerciseName }
                    groupedLogs.forEach { (exName, list) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = exName,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Print neat sets row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                list.forEach { sLog ->
                                    val logLabel = if (sLog.isDropset) "DS" else "S${sLog.setIndex}"
                                    Box(
                                        modifier = Modifier
                                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(4.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(4.dp))
                                            .background(BorderColorSubtle)
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "$logLabel: ${sLog.weightKg}kg x${sLog.reps}",
                                            fontSize = 9.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

// Generate Calendar Days list for grid
fun getCalendarMonthGridDates(calendar: Calendar): List<Calendar?> {
    val list = mutableListOf<Calendar?>()
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday
    // Add null items for offset weeks
    for (i in 1 until firstDayOfWeek) {
        list.add(null)
    }

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDays) {
        val dateCal = cal.clone() as Calendar
        dateCal.set(Calendar.DAY_OF_MONTH, i)
        list.add(dateCal)
    }

    // Pad with nulls to multiple of 7 to prevent stretching on the last row
    while (list.size % 7 != 0) {
        list.add(null)
    }

    return list
}
