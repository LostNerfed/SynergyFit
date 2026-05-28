package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FitSettings
import com.example.data.database.Routine
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: FitnessViewModel,
    onNavigateToSettings: () -> Unit,
    onStartActiveWorkout: (Routine) -> Unit,
    onStartCustomWorkout: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val settings by viewModel.settingsState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()
    val selectedDateMeals by viewModel.selectedDateMeals.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    var showPersonalizeSheet by remember { mutableStateOf(false) }
    var showStartWorkoutSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Calculate metrics
    val consumedCalories = selectedDateMeals.sumOf { it.totalCalories }
    val targetCalories = settings.targetCalories
    val calRatio = if (targetCalories > 0) consumedCalories.toFloat() / targetCalories else 0f

    // Training volume for today/all logs
    val totalVolume = allLogs.sumOf { it.weightKg * it.reps }
    val volumeTarget = 10000.0 // target volume in kg
    val volRatio = (totalVolume / volumeTarget).toFloat().coerceIn(0f, 1f)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBg),
        containerColor = AmoledBg,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                            text = "Hola, ${settings.username}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            style = androidx.compose.ui.text.TextStyle(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Color.White, Color(0xFF00E5FF))
                                )
                            )
                        )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Contador Mensual de Entrenamientos
                        val currentMonthValue = java.time.LocalDate.now().monthValue
                        val currentYearValue = java.time.LocalDate.now().year
                        val monthlyWorkouts = sessions.count {
                            val d = java.time.Instant.ofEpochMilli(it.dateMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            d.monthValue == currentMonthValue && d.year == currentYearValue
                        }
                        
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E676).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$monthlyWorkouts en el mes", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { showPersonalizeSheet = true },
                            modifier = Modifier
                                .background(Color(0xFFB388FF).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(0xFFB388FF).copy(alpha = 0.5f), CircleShape)
                                .size(40.dp)
                                .testTag("personalize_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Personalizar IA",
                                tint = Color(0xFFB388FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .background(com.example.ui.theme.AmoledSurface, CircleShape).border(1.dp, com.example.ui.theme.PremiumGradientBorder, CircleShape)
                                .size(40.dp)
                                .testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes Generales",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Fila 1: Nutrición y Racha (Grid 2x2)
            item {
                val today = java.time.LocalDate.now()
                val logsByDate = sessions.groupBy { 
                    java.time.Instant.ofEpochMilli(it.dateMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDate() 
                }
                
                var currentStreak = 0
                var checkDate = today
                if (!logsByDate.containsKey(today) && logsByDate.containsKey(today.minusDays(1))) {
                    checkDate = today.minusDays(1)
                }
                while (logsByDate.containsKey(checkDate)) {
                    currentStreak++
                    checkDate = checkDate.minusDays(1)
                }

                val dayOfWeek = today.dayOfWeek.value // 1 = Monday, 7 = Sunday
                val startOfWeek = today.minusDays((dayOfWeek - 1).toLong())

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta A: Calorías (1x1)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawCircle(color = BorderColorSubtle, style = Stroke(width = 10.dp.toPx()))
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = calRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "$consumedCalories", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(text = "kcal", fontSize = 10.sp, color = TextSecundario)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Calorías", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecundario)
                    }

                    // Tarjeta B: Racha y Volumen (1x1)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(24.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header: Flame + Streak | Icon
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "Racha", tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(text = "RACHA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecundario, letterSpacing = 1.sp)
                                    Text(text = "$currentStreak DÍAS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Entrenamiento", tint = TextSecundario, modifier = Modifier.size(16.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Weekly dots
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val daysInitials = listOf("L", "M", "M", "J", "V", "S", "D")
                            (0..6).forEach { offset ->
                                val date = startOfWeek.plusDays(offset.toLong())
                                val trained = logsByDate.containsKey(date)
                                val isToday = date == today
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                color = if (trained) Color(0xFF00E676) else if (isToday) Color.Transparent else BorderColorSubtle,
                                                shape = CircleShape
                                            )
                                            .border(
                                                width = if (isToday && !trained) 1.5.dp else 0.dp,
                                                color = if (isToday && !trained) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (trained) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AmoledBg, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = daysInitials[offset], fontSize = 8.sp, color = TextSecundario)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Volume Progress
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(text = "VOLUMEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecundario, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(text = "${totalVolume.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "/10k", fontSize = 10.sp, color = TextSecundario, modifier = Modifier.padding(bottom = 2.dp))
                                }
                                Text(text = "${(volRatio * 100).toInt()}%", fontSize = 10.sp, color = TextSecundario, modifier = Modifier.padding(bottom = 2.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = volRatio,
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                color = Color(0xFF00E676),
                                trackColor = BorderColorSubtle,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // 1x1 Cards Row: Body Weight & Start Workout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    var isEditingWeight by remember { mutableStateOf(false) }
                    var weightInput by remember { mutableStateOf(settings.bodyWeight.toString()) }
                    var isSavedFeedback by remember { mutableStateOf(false) }

                    LaunchedEffect(settings.bodyWeight) {
                        weightInput = settings.bodyWeight.toString()
                    }

                    // Body Weight 1x1 Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(24.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    if (isEditingWeight) {
                                        val wt = weightInput.toDoubleOrNull() ?: settings.bodyWeight
                                        viewModel.updateSettings(settings.copy(bodyWeight = wt))
                                    }
                                    isEditingWeight = !isEditingWeight
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isEditingWeight) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = if (isEditingWeight) "Guardar peso" else "Editar peso",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (!isEditingWeight) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${settings.bodyWeight}",
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "kg",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecundario,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                        modifier = Modifier
                                            .width(75.dp),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                val wt = weightInput.toDoubleOrNull() ?: settings.bodyWeight
                                                viewModel.updateSettings(settings.copy(bodyWeight = wt))
                                                isEditingWeight = false
                                            }
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "kg",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecundario,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Peso",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Corporal",
                                fontSize = 11.sp,
                                color = TextSecundario,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Start Workout 1x1 Card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(24.dp))
                            .border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(24.dp))
                            .clickable { showStartWorkoutSheet = true }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Iniciar",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Iniciar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rutina",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Coach Chat module
            item {
                var chatInput by remember { mutableStateOf("") }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(16.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Coach AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Coach AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Conversational Bubble FIFO elements
                    if (chatHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pregúntale a la IA",
                                color = TextSecundario,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            chatHistory.forEach { (q, r) ->
                                // Question Bubble Right aligned
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .background(BorderColorSubtle, RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(text = q, color = Color.White, fontSize = 12.sp)
                                    }
                                }

                                // Answer Bubble Left aligned
                                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(text = r, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (chatLoading) {
                        LinearProgressIndicator(
                            color = Color.White,
                            trackColor = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("Pregunta algo al Coach...", color = TextSecundario, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("coach_chat_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                cursorColor = Color.White,
                                focusedContainerColor = com.example.ui.theme.AmoledSurface,
                                unfocusedContainerColor = com.example.ui.theme.AmoledSurface),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (chatInput.trim().isNotEmpty()) {
                                    viewModel.askCoach(chatInput)
                                    chatInput = ""
                                }
                            },
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .size(56.dp)
                                .testTag("send_question_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Bottom Sheet: Customize IA / Coach Setup
        if (showPersonalizeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPersonalizeSheet = false },
                containerColor = AmoledSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
            ) {
                CoachSetupContent(
                    settings = settings,
                    onSave = { updated ->
                        viewModel.updateSettings(updated)
                        showPersonalizeSheet = false
                    }
                )
            }
        }

        // Bottom Sheet: Select Saved Routine to Start
        if (showStartWorkoutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showStartWorkoutSheet = false },
                containerColor = AmoledSurface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
            ) {
                StartWorkoutMenuContent(
                    routines = routines,
                    onSelectRoutine = { r ->
                        showStartWorkoutSheet = false
                        onStartActiveWorkout(r)
                    },
                    onSelectCustom = {
                        showStartWorkoutSheet = false
                        onStartCustomWorkout()
                    }
                )
            }
        }
    }
}

@Composable
fun CoachSetupContent(
    settings: FitSettings,
    onSave: (FitSettings) -> Unit
) {
    var selectedProvider by remember { mutableStateOf(settings.iaProvider) }
    var apiKeyInput by remember { mutableStateOf(settings.apiKey) }
    var selectedGoal by remember { mutableStateOf(settings.fitnessGoal) }
    var targetCaloriesInput by remember { mutableStateOf(settings.targetCalories.toString()) }

    val goalsList = listOf("Hipertrofia", "Pérdida de grasa", "Fuerza máxima", "Resistencia muscular", "Recomposición física", "Salud General")
    val providers = listOf("Gemini", "DeepSeek", "Groq")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Personalizar Coach de IA",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Configura la inteligencia artificial de SynergyFit para adecuar los consejos a tus objetivos.",
            fontSize = 12.sp,
            color = TextSecundario,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Provider Selector
        Text(text = "Proveedor de IA", fontSize = 13.sp, color = TextSecundario)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            providers.forEach { provider ->
                val isSelected = provider == selectedProvider
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedProvider = provider }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = provider,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // API Key Input
        Text(text = "Clave API (API Key)", fontSize = 13.sp, color = TextSecundario)
        OutlinedTextField(
            value = apiKeyInput,
            onValueChange = { apiKeyInput = it },
            placeholder = { Text("Introduce tu clave api...", color = TextSecundario, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("api_key_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                cursorColor = Color.White,
                focusedContainerColor = com.example.ui.theme.AmoledSurface,
                unfocusedContainerColor = com.example.ui.theme.AmoledSurface),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fitness Goal Selector
        Text(text = "Objetivo Fitness Principal", fontSize = 13.sp, color = TextSecundario)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // First 3 goals
            goalsList.take(3).forEach { goal ->
                val isSelected = goal == selectedGoal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedGoal = goal }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Remaining 3 goals
            goalsList.takeLast(3).forEach { goal ->
                val isSelected = goal == selectedGoal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color.White else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.White else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedGoal = goal }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Target Calories Input
        Text(text = "Meta de Calorías Diarias (kcal)", fontSize = 13.sp, color = TextSecundario)
        OutlinedTextField(
            value = targetCaloriesInput,
            onValueChange = { targetCaloriesInput = it },
            placeholder = { Text("Ej. 2500", color = TextSecundario, fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("target_calories_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                cursorColor = Color.White,
                focusedContainerColor = com.example.ui.theme.AmoledSurface,
                unfocusedContainerColor = com.example.ui.theme.AmoledSurface),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val cal = targetCaloriesInput.toIntOrNull() ?: settings.targetCalories
                onSave(
                    settings.copy(
                        iaProvider = selectedProvider,
                        apiKey = apiKeyInput,
                        fitnessGoal = selectedGoal,
                        targetCalories = cal
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_coach_setup_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Guardar Cambios", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StartWorkoutMenuContent(
    routines: List<Routine>,
    onSelectRoutine: (Routine) -> Unit,
    onSelectCustom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Iniciar Sesión de Entrenamiento",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Selecciona una plantilla de rutina para cargar tus series y comenzar a registrar.",
            fontSize = 12.sp,
            color = TextSecundario,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Option 1: Custom quick workout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectCustom() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Custom Quick",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Sesión Libre / Sin Plantilla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = "Registrar ejercicio a ejercicio en el momento",
                    fontSize = 12.sp,
                    color = TextSecundario
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tus Plantillas de Rutinas",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecundario,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tienes plantillas creadas. Ve a la sección de Rutinas para agregar una nueva.",
                    color = TextSecundario,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(routines) { routine ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectRoutine(routine) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(com.example.ui.theme.AmoledSurface, CircleShape).border(1.dp, com.example.ui.theme.PremiumGradientBorder, CircleShape)
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = "Routine",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = routine.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                if (routine.description.isNotEmpty()) {
                                    Text(
                                        text = routine.description,
                                        fontSize = 11.sp,
                                        color = TextSecundario
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Start",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
