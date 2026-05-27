package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Session
import com.example.data.database.SessionLog
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(
    viewModel: FitnessViewModel
) {
    val sessions by viewModel.sessions.collectAsState()
    val logs by viewModel.allLogs.collectAsState()

    // 1. Calculate Grid 2x2 Stats
    val totalVolume = logs.sumOf { it.weightKg * it.reps }
    val uniqueExercisesCount = logs.map { it.exerciseName.lowercase().trim() }.distinct().size

    // Weekly session count
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val oneWeekAgo = calendar.timeInMillis
    val weeklySessionsCount = sessions.filter { it.dateMillis >= oneWeekAgo }.size

    // Active days and streaks
    val datesTrained = sessions.map {
        SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(Date(it.dateMillis))
    }.distinct().sortedDescending()

    val activeDaysCount = datesTrained.size
    val activeStreak = calculateStreak(datesTrained)

    // 2. Personal Records (Based on Volume: weight x reps)
    val personalRecordsMap = remember(logs) {
        val records = mutableMapOf<String, RecordData>()
        logs.forEach { log ->
            val exName = log.exerciseName
            val existing = records[exName]
            val currentVolume = log.weightKg * log.reps
            val existingVolume = if (existing != null) existing.weight * existing.reps else 0.0
            if (existing == null || currentVolume > existingVolume) {
                records[exName] = RecordData(
                    weight = log.weightKg,
                    reps = log.reps,
                    dateMillis = sessions.firstOrNull { it.id == log.sessionId }?.dateMillis ?: System.currentTimeMillis()
                )
            }
        }
        records.entries.toList().sortedByDescending { it.value.weight * it.value.reps }
    }

    // 3. Exercise lists
    val exercisesGroupedByLogName = remember(logs) {
        logs.groupBy { it.exerciseName }
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
                text = "Progreso & Analíticas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 2x2 Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Racha Actual",
                        value = "$activeStreak días",
                        sub = "Racha de entrenamientos",
                        icon = Icons.Default.Whatshot,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Sesiones Semanales",
                        value = "$weeklySessionsCount",
                        sub = "Últimos 7 días",
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Volumen Total",
                        value = if (totalVolume >= 1000) String.format(Locale.getDefault(), "%.1f T", totalVolume / 1000) else "${totalVolume.toInt()} kg",
                        sub = "Suma histórica",
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Ejercicios",
                        value = "$uniqueExercisesCount",
                        sub = "Ejercicios aprendidos",
                        icon = Icons.Default.FormatListBulleted,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Personal Records Section
        item {
            Text(
                text = "Récords Personales (PRs)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        if (personalRecordsMap.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Completa entrenamientos para ver tus records.", color = TextSecundario, fontSize = 12.sp)
                }
            }
        } else {
            items(personalRecordsMap.take(5)) { (name, record) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale("es", "ES")).format(Date(record.dateMillis))
                        Text(text = dateStr, fontSize = 11.sp, color = TextSecundario)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BorderColorSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${record.weight} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "x ${record.reps} reps",
                            fontSize = 12.sp,
                            color = TextSecundario,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Historial segment per exercise (Expandable lists)
        item {
            Text(
                text = "Historial por Ejercicio",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        if (exercisesGroupedByLogName.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Aún no has registrado ejercicios.", color = TextSecundario, fontSize = 12.sp)
                }
            }
        } else {
            items(exercisesGroupedByLogName.keys.toList()) { exerciseName ->
                var isExpanded by remember { mutableStateOf(false) }
                val currentLogs = exercisesGroupedByLogName[exerciseName] ?: emptyList()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isExpanded = !isExpanded }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exerciseName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Registrado ${currentLogs.size} veces",
                                fontSize = 11.sp,
                                color = TextSecundario
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = Color.White
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Simple linear chart of weights!
                            val displayRecordWeights = currentLogs.map { it.weightKg }.takeLast(10)
                            if (displayRecordWeights.size > 1) {
                                Text(
                                    text = "Tendencia de Peso (Últimas series)",
                                    fontSize = 11.sp,
                                    color = TextSecundario,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                ProgressLineSparkline(weights = displayRecordWeights)
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Individual logged set rows
                            currentLogs.takeLast(15).reversed().forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BorderColorSubtle, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (log.isDropset) "Dropset" else "S${log.setIndex}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (log.isDropset) Color.White else TextSecundario
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${log.weightKg} kg x ${log.reps} reps",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }

                                    if (log.isDropset) {
                                        Box(
                                            modifier = Modifier
                                                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "DROPSET", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
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

@Composable
fun StatCard(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 11.sp, color = TextSecundario, fontWeight = FontWeight.Medium)
            Icon(imageVector = icon, contentDescription = title, tint = TextSecundario, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = sub, fontSize = 9.sp, color = TextSecundario)
    }
}

// Draw a simple sparkline on Canvas for the exercise trend
@Composable
fun ProgressLineSparkline(weights: List<Double>) {
    val maxWeight = weights.maxOrNull() ?: 1.0
    val minWeight = weights.minOrNull() ?: 0.0
    val weightDiff = (maxWeight - minWeight).coerceAtLeast(1.0)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        val width = size.width
        val height = size.height
        val points = mutableListOf<Offset>()

        weights.forEachIndexed { idx, wt ->
            val fractionX = idx.toFloat() / (weights.size - 1).coerceAtLeast(1)
            val x = fractionX * width

            val fractionY = (wt - minWeight) / weightDiff
            // Invert Y direction
            val y = height - (fractionY.toFloat() * height * 0.8f) - (height * 0.1f)
            points.add(Offset(x, y))
        }

        // Draw connections
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.White,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

// Calculate streak based on consecutive unique dates
fun calculateStreak(dates: List<String>): Int {
    if (dates.isEmpty()) return 0
    val format = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
    val parsedDates = dates.mapNotNull {
        try { format.parse(it) } catch (e: Exception) { null }
    }.sortedDescending()

    if (parsedDates.isEmpty()) return 0

    var streak = 0
    val calMock = Calendar.getInstance()
    // Check if the latest completed training is today or yesterday
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val lastTrained = Calendar.getInstance()
    lastTrained.time = parsedDates[0]
    lastTrained.set(Calendar.HOUR_OF_DAY, 0)
    lastTrained.set(Calendar.MINUTE, 0)
    lastTrained.set(Calendar.SECOND, 0)
    lastTrained.set(Calendar.MILLISECOND, 0)

    val diff = (today.timeInMillis - lastTrained.timeInMillis) / (1000 * 60 * 60 * 24)
    if (diff > 1) {
        return 0 // Streak broken because they didn't train today or yesterday
    }

    streak = 1
    calMock.time = parsedDates[0]

    for (i in 1 until parsedDates.size) {
        val prevDay = Calendar.getInstance()
        prevDay.time = parsedDates[i]

        calMock.add(Calendar.DAY_OF_YEAR, -1)

        val isConsecutive = (calMock.get(Calendar.YEAR) == prevDay.get(Calendar.YEAR)) &&
                (calMock.get(Calendar.DAY_OF_YEAR) == prevDay.get(Calendar.DAY_OF_YEAR))

        if (isConsecutive) {
            streak++
        } else {
            break
        }
    }

    return streak
}

data class RecordData(
    val weight: Double,
    val reps: Int,
    val dateMillis: Long
)
