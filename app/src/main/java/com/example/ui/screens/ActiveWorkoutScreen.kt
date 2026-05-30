package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Session
import com.example.data.database.SessionLog
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    viewModel: FitnessViewModel,
    onFinish: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val activeLogs by viewModel.activeLogs.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    var backdateOffsetDays by remember { mutableStateOf(0) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showSaveWarnDialog by remember { mutableStateOf(false) }
    var tempAddExerciseName by remember { mutableStateOf("") }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    val currentSession = activeSession
    if (currentSession == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Cargando sesión activa...", color = Color.White)
        }
        return
    }

    // Cronometer display
    val cronometerStr = remember(elapsedSeconds) {
        val h = elapsedSeconds / 3600
        val m = (elapsedSeconds % 3600) / 60
        val s = elapsedSeconds % 60
        if (h > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }
    }

    // Group logs by exercise name
    val groupedLogs = remember(activeLogs) {
        activeLogs.groupBy { it.exerciseName }
    }

    BackHandler {
        showDiscardDialog = true
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .imePadding(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentSession.routineName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color.Red, RoundedCornerShape(3.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "En Vivo: $cronometerStr",
                                fontSize = 12.sp,
                                color = TextSecundario,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showDiscardDialog = true },
                        modifier = Modifier.testTag("discard_workout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancelar", tint = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Backdate picker
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassModifier(RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Fecha de Registro",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(0 to "Hoy", 1 to "Ayer", 2 to "Hace 2 días").forEach { (offset, label) ->
                                val isSelected = backdateOffsetDays == offset
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
                                        .clickable { backdateOffsetDays = offset }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Exercise cards
                items(groupedLogs.keys.toList(), key = { it }) { exName ->
                    val logsForEx = groupedLogs[exName] ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            // Add exercise set shortcut
                            IconButton(onClick = { viewModel.addActiveSet(exName) }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Suma Serie", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Column heads
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(text = "SERIE", modifier = Modifier.width(60.dp), fontSize = 10.sp, color = TextSecundario, fontWeight = FontWeight.Bold)
                            Text(text = "KG", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextSecundario, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Text(text = "REPS", modifier = Modifier.weight(1f), fontSize = 10.sp, color = TextSecundario, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.width(36.dp)) // space for delete action
                        }

                        logsForEx.forEach { sLog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Label
                                Box(
                                    modifier = Modifier.width(60.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (sLog.isDropset) {
                                            Icon(imageVector = Icons.Default.SubdirectoryArrowRight, contentDescription = "Dropset", tint = TextSecundario, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "DS", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(text = "S${sLog.setIndex}", fontSize = 12.sp, color = TextSecundario, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // KG Input
                                var weightText by remember(sLog.id) { mutableStateOf(if (sLog.weightKg > 0) sLog.weightKg.toString() else "") }
                                LaunchedEffect(weightText) {
                                    delay(400)
                                    val parseWt = weightText.toDoubleOrNull() ?: 0.0
                                    if (parseWt != sLog.weightKg) {
                                        viewModel.updateActiveSetWeight(sLog.id, parseWt)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .liquidGlassModifier(RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = weightText,
                                        onValueChange = { input ->
                                            weightText = input
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            fontSize = 15.sp
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Reps Input
                                var repsText by remember(sLog.id) { mutableStateOf(if (sLog.reps > 0) sLog.reps.toString() else "") }
                                LaunchedEffect(repsText) {
                                    delay(400)
                                    val parseReps = repsText.toIntOrNull() ?: 0
                                    if (parseReps != sLog.reps) {
                                        viewModel.updateActiveSetReps(sLog.id, parseReps)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .liquidGlassModifier(RoundedCornerShape(8.dp))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.foundation.text.BasicTextField(
                                        value = repsText,
                                        onValueChange = { input ->
                                            repsText = input
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            fontSize = 15.sp
                                         ),
                                         cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                         modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Delete Set Button
                                IconButton(
                                    onClick = { viewModel.deleteActiveSetLog(sLog.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Set", tint = TextSecundario, modifier = Modifier.size(14.dp))
                                }
                            }

                            // Add Dropset Button (Only visible for non-dropset rows)
                            if (!sLog.isDropset) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { viewModel.addActiveDropset(exName, sLog.setIndex) },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Dropset", modifier = Modifier.size(10.dp), tint = TextSecundario)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Añadir Dropset", fontSize = 10.sp, color = TextSecundario)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderColorSubtle)
                        )
                    }
                }

                // Add random exercise button
                item {
                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("add_exercise_to_active_button"),
                        border = BorderStroke(1.dp, com.example.ui.theme.BorderColorSubtle),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Añadir Ejercicio Alternativo", fontSize = 13.sp)
                        }
                    }
                }

                // Finish Workout prominent bottom button
                item {
                    Button(
                        onClick = {
                            // Check if there are any incomplete sets
                            val isIncomplete = activeLogs.any { it.weightKg <= 0 || it.reps <= 0 }
                            if (isIncomplete) {
                                showSaveWarnDialog = true
                            } else {
                                viewModel.finishActiveWorkout(backdateOffsetDays) {
                                    onFinish()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("finish_workout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Finalizar Entrenamiento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        // Dialogs: Discard workout confirmation
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text(text = "¿Cancelar Entrenamiento?") },
                text = { Text(text = "Se perderán todos los datos ingresados en esta sesión en vivo. ¿Deseas descartar?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.discardActiveWorkout()
                            showDiscardDialog = false
                            onFinish()
                        }
                    ) {
                        Text(text = "Descartar", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text(text = "Volver", color = Color.White)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xF0040A18),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }

        // Dialogs: Empty sets Warning
        if (showSaveWarnDialog) {
            AlertDialog(
                onDismissRequest = { showSaveWarnDialog = false },
                title = { Text(text = "Series Incompletas") },
                text = { Text(text = "Tienes series en 0 o incompletas de tu entrenamiento actual. Estas no se registrarán en tu historial. ¿Quieres guardar de todas formas?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSaveWarnDialog = false
                            viewModel.finishActiveWorkout(backdateOffsetDays) {
                                onFinish()
                            }
                        }
                    ) {
                        Text(text = "Guardar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveWarnDialog = false }) {
                        Text(text = "Corregir", color = TextSecundario)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xF0040A18),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }

        // Dialogs: Add exercise in real time
        if (showAddExerciseDialog) {
            val allExercises by viewModel.allExercises.collectAsState()
            var searchInSuperior by remember { mutableStateOf(true) }

            val superiorExercises = remember(allExercises) {
                allExercises.filter { 
                    val cat = it.category.lowercase()
                    cat.contains("superior") || cat.contains("torso") || cat.contains("pecho") || 
                    cat.contains("espalda") || cat.contains("hombro") || cat.contains("bíceps") || 
                    cat.contains("tríceps") || cat.contains("brazo") || 
                    (!cat.contains("inferior") && !cat.contains("pierna") && !cat.contains("femoral") && 
                     !cat.contains("cuádriceps") && !cat.contains("glúteo") && !cat.contains("gemelo"))
                }
            }

            val inferiorExercises = remember(allExercises) {
                allExercises.filter { 
                    val cat = it.category.lowercase()
                    cat.contains("inferior") || cat.contains("pierna") || cat.contains("femoral") || 
                    cat.contains("cuádriceps") || cat.contains("glúteo") || cat.contains("gemelo")
                }
            }

            val targetList = if (searchInSuperior) superiorExercises else inferiorExercises

            AlertDialog(
                onDismissRequest = { showAddExerciseDialog = false },
                title = { Text(text = "Nuevo Ejercicio", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Selecciona de tu lista o escribe uno de manera manual:", fontSize = 11.sp, color = TextSecundario)
                        
                        // Category Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                        .border(
                                            1.dp, 
                                            if (searchInSuperior) Color.White else BorderColor, 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(if (searchInSuperior) BorderColorSubtle else Color.Transparent)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { searchInSuperior = true }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Parte Superior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                        .border(
                                            1.dp, 
                                            if (!searchInSuperior) Color.White else BorderColor, 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(if (!searchInSuperior) BorderColorSubtle else Color.Transparent)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { searchInSuperior = false }
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Parte Inferior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Horizontal Quick Select of Exercises
                        if (targetList.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "Ejercicios guardados para rápido acceso:", fontSize = 11.sp, color = TextSecundario)
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(targetList) { exObj ->
                                        Box(
                                            modifier = Modifier
                                                .background(BorderColorSubtle, RoundedCornerShape(8.dp))
                                                .liquidGlassModifier(RoundedCornerShape(8.dp))
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { tempAddExerciseName = exObj.name }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(text = exObj.name, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(text = "(Lista vacía en esta categoría)", fontSize = 10.sp, color = TextSecundario)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = tempAddExerciseName,
                            onValueChange = { tempAddExerciseName = it },
                            placeholder = { Text("Manual") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().liquidGlassModifier(RoundedCornerShape(12.dp)),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                cursorColor = Color.White
                            , focusedContainerColor = Color(0x05FFFFFF), unfocusedContainerColor = Color(0x05FFFFFF))
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (tempAddExerciseName.trim().isNotEmpty()) {
                                val selectedCat = if (searchInSuperior) "Parte Superior" else "Parte Inferior"
                                viewModel.addExerciseToActiveWorkout(tempAddExerciseName, selectedCat)
                                tempAddExerciseName = ""
                            }
                        }
                    ) {
                        Text(text = "Añadir", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            tempAddExerciseName = ""
                            showAddExerciseDialog = false
                        }
                    ) {
                        Text(text = "Aplicar", color = TextSecundario)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xF0040A18),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}
