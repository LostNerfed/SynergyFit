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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showStartWorkoutSheet = true },
                containerColor = Color.White,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 76.dp) // clear bottom navigation bar height
                    .testTag("start_workout_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Workout",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hola, ${settings.username}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tu enfoque de hoy: ${settings.fitnessGoal}",
                            fontSize = 13.sp,
                            color = TextSecundario
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showPersonalizeSheet = true },
                            modifier = Modifier
                                .border(1.dp, BorderColor, CircleShape)
                                .size(40.dp)
                                .testTag("personalize_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Personalizar IA",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .border(1.dp, BorderColor, CircleShape)
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

            // Circular progress meters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Circle 1: Calories
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawCircle(
                                    color = BorderColorSubtle,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = calRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$consumedCalories",
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "kcal",
                                    fontSize = 10.sp,
                                    color = TextSecundario
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Calorías",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecundario
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .width(1.dp)
                            .background(BorderColor)
                    )

                    // Circle 2: Volume
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                            Canvas(modifier = Modifier.size(80.dp)) {
                                drawCircle(
                                    color = BorderColorSubtle,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    color = Color.White,
                                    startAngle = -90f,
                                    sweepAngle = volRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val displayVol = if (totalVolume >= 1000) String.format(Locale.getDefault(), "%.1fk", totalVolume/1000) else "${totalVolume.toInt()}"
                                Text(
                                    text = displayVol,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "kg",
                                    fontSize = 10.sp,
                                    color = TextSecundario
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registro Semanal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecundario
                        )
                    }
                }
            }

            // Body Weight Tracker Card
            item {
                var isEditingWeight by remember { mutableStateOf(false) }
                var weightInput by remember { mutableStateOf(settings.bodyWeight.toString()) }
                var isSavedFeedback by remember { mutableStateOf(false) }

                LaunchedEffect(settings.bodyWeight) {
                    weightInput = settings.bodyWeight.toString()
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Peso Corporal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isEditingWeight) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        isEditingWeight = true
                                    },
                                    onClick = {
                                        // Give them feedback or allow standard click too if needed, but per request we emphasize long-press.
                                        // We can do a small hint
                                    }
                                )
                                .border(1.dp, BorderColorSubtle, RoundedCornerShape(12.dp))
                                .background(AmoledSurface)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${settings.bodyWeight} kg",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    if (isSavedFeedback) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "¡Guardado!",
                                            color = Color.Green,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Mantén presionado para editar",
                                    fontSize = 12.sp,
                                    color = TextSecundario
                                )
                            }
                            IconButton(
                                onClick = { isEditingWeight = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar peso",
                                    tint = TextSecundario,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                suffix = { Text("kg", color = TextSecundario) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("weight_input_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val wt = weightInput.toDoubleOrNull() ?: settings.bodyWeight
                                    viewModel.updateSettings(settings.copy(bodyWeight = wt))
                                    isSavedFeedback = true
                                    isEditingWeight = false
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        isSavedFeedback = false
                                    }
                                },
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("save_weight_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Guardar")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    weightInput = settings.bodyWeight.toString()
                                    isEditingWeight = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = TextSecundario
                                )
                            }
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
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
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
                            fontSize = 16.sp,
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
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pregúntale a la IA",
                                color = TextSecundario,
                                fontSize = 12.sp,
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
                                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp))
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
                                .height(50.dp)
                                .testTag("coach_chat_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = BorderColor,
                                cursorColor = Color.White
                            )
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
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .size(50.dp)
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
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
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

        Spacer(modifier = Modifier.height(16.dp))

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
                focusedBorderColor = Color.White,
                unfocusedBorderColor = BorderColor,
                cursorColor = Color.White
            )
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
                focusedBorderColor = Color.White,
                unfocusedBorderColor = BorderColor,
                cursorColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(30.dp))

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
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
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
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable { onSelectRoutine(routine) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, BorderColor, CircleShape)
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
