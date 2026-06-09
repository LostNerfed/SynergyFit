package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.data.database.PlanExercise
import com.example.data.database.Routine
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDaysScreen(
    viewModel: FitnessViewModel,
    onStartRoutineWorkout: (Routine) -> Unit
) {
    val routines by viewModel.routines.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Import Launcher
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonStr = inputStream?.bufferedReader()?.use { it.readText() }
                    if (jsonStr != null) {
                        val result = viewModel.importRoutinesJson(jsonStr)
                        Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al leer archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val jsonStr = viewModel.exportRoutinesJson()
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonStr.toByteArray())
                    }
                    Toast.makeText(context, "Rutinas exportadas exitosamente", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al guardar archivo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Sub-UI state routing
    var selectedRoutineForModelDetails by remember { mutableStateOf<Routine?>(null) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }
    var activePlanTab by remember { mutableStateOf(0) } // 0 = Tutinas, 1 = Lista Madre de Ejercicios
    var showAddCustomExerciseDialog by remember { mutableStateOf(false) }

    // Handle system back button when deep in routine details
    BackHandler(enabled = selectedRoutineForModelDetails != null) {
        selectedRoutineForModelDetails = null
    }

    // Categorization logic helper
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        val selectedRoutine = selectedRoutineForModelDetails
        if (selectedRoutine == null) {
            // --- MAIN TABS (Rutinas vs Lista Madre) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassModifier(RoundedCornerShape(32.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activePlanTab == 0) Color.White.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { activePlanTab = 0 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mis Rutinas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activePlanTab == 0) Color.White else TextSecundario
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activePlanTab == 1) Color.White.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .clickable { activePlanTab = 1 }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lista de Ejercicios",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activePlanTab == 1) Color.White else TextSecundario
                    )
                }
            }

            if (activePlanTab == 0) {
                // --- CATEGORY 1: PLAN DAYS VIEW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tus Rutinas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Plantillas de entrenamiento programadas",
                            fontSize = 11.sp,
                            color = TextSecundario
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier
                                .liquidGlassModifier(CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Importar Rutinas", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { exportLauncher.launch("mis_rutinas.json") },
                            modifier = Modifier
                                .liquidGlassModifier(CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = "Exportar Rutinas", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showCreateRoutineDialog = true },
                            modifier = Modifier
                                .liquidGlassModifier(CircleShape)
                                .size(36.dp)
                                .testTag("add_routine_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine", tint = Color.White)
                        }
                    }
                }

                if (routines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .liquidGlassModifier(RoundedCornerShape(12.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes rutinas programadas.\nCrea tu primera plantilla del día aquí arriba.",
                            color = TextSecundario,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(routines) { routine ->
                            RoutineRowItem(
                                routine = routine,
                                viewModel = viewModel,
                                onClick = { selectedRoutineForModelDetails = routine },
                                onStartWorkout = { onStartRoutineWorkout(routine) },
                                onDelete = { viewModel.deleteRoutine(routine) }
                            )
                        }
                    }
                }
            } else {
                // --- CATEGORY 2: LISTA MADRE VIEW ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ejercicios",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Tus ejercicios guardados para usar",
                            fontSize = 11.sp,
                            color = TextSecundario
                        )
                    }

                    IconButton(
                        onClick = { showAddCustomExerciseDialog = true },
                        modifier = Modifier
                            .liquidGlassModifier(CircleShape)
                            .size(36.dp)
                            .testTag("add_custom_exercise_mother_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Ejercicio", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- SECCIÓN PARTE SUPERIOR ---
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PARTE SUPERIOR (Torso / Brazos)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (superiorExercises.isEmpty()) {
                        item {
                            Text(
                                text = "Sin ejercicios guardados en esta categoría.",
                                color = TextSecundario,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        items(superiorExercises) { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassModifier(RoundedCornerShape(10.dp))
                                    .liquidGlassModifier(RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = ex.name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Categoría: ${ex.category}", fontSize = 10.sp, color = TextSecundario)
                                }
                                IconButton(onClick = { viewModel.deleteCustomExercise(ex) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = TextSecundario, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // --- SECCIÓN PARTE INFERIOR ---
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PARTE INFERIOR (Pierna / Abdomen)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (inferiorExercises.isEmpty()) {
                        item {
                            Text(
                                text = "Sin ejercicios guardados en esta categoría.",
                                color = TextSecundario,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        items(inferiorExercises) { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .liquidGlassModifier(RoundedCornerShape(10.dp))
                                    .liquidGlassModifier(RoundedCornerShape(10.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = ex.name, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Categoría: ${ex.category}", fontSize = 10.sp, color = TextSecundario)
                                }
                                IconButton(onClick = { viewModel.deleteCustomExercise(ex) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = TextSecundario, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // --- DIALOG: ADD CUSTOM EXERCISE TO MOTHER LIST ---
            if (showAddCustomExerciseDialog) {
                var newExName by remember { mutableStateOf("") }
                var isSuperiorSelected by remember { mutableStateOf(true) }

                AlertDialog(
                    onDismissRequest = { showAddCustomExerciseDialog = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text(text = "Nuevo Ejercicio", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Añade un ejercicio a tu lista general.", fontSize = 11.sp, color = TextSecundario)

                            OutlinedTextField(
                                value = newExName,
                                onValueChange = { newExName = it },
                                label = { Text("Nombre del Ejercicio", color = TextSecundario) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    cursorColor = Color.White,
                                    focusedContainerColor = Color(0x05FFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF))
                            )

                            Text(text = "Grupo muscular:", fontSize = 11.sp, color = TextSecundario, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (isSuperiorSelected) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                        .background(if (isSuperiorSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { isSuperiorSelected = true }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Superior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (!isSuperiorSelected) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                        .background(if (!isSuperiorSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { isSuperiorSelected = false }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Inferior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newExName.trim().isNotEmpty()) {
                                    val cat = if (isSuperiorSelected) "Parte Superior" else "Parte Inferior"
                                    viewModel.addCustomExercise(newExName, cat)
                                    newExName = ""
                                    showAddCustomExerciseDialog = false
                                }
                            }
                        ) {
                            Text(text = "Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCustomExerciseDialog = false }) {
                            Text(text = "Cancelar", color = TextSecundario)
                        }
                    },
                    containerColor = Color(0xFA040A18),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }

        } else {
            // --- DAY TEMPLATE VIEW (Selected Routine Details) ---
            val activeDetailedRoutine = selectedRoutine
            val routineExercises by viewModel.getExercisesForRoutine(activeDetailedRoutine.id).collectAsState(initial = emptyList())

            var showAddExerciseDialogInDetails by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedRoutineForModelDetails = null },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeDetailedRoutine.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(text = "Editar Serie y Ejercicios", fontSize = 11.sp, color = TextSecundario)
                }
            }

            // Quick action to start workout immediately
            Button(
                onClick = { onStartRoutineWorkout(activeDetailedRoutine) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_workout_from_routine_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Iniciar Sesión Activa", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = BorderColor)

            // Exercises editing list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(routineExercises) { ex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassModifier(RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = ex.exerciseName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )

                            // Target set counts
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Series Objetivo: ${ex.targetSets}",
                                    fontSize = 12.sp,
                                    color = TextSecundario
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                // Quick action to adjust target set counts
                                Box(
                                    modifier = Modifier
                                        .background(BorderColorSubtle, RoundedCornerShape(4.dp))
                                        .clickable {
                                            val nextSets = if (ex.targetSets >= 10) 1 else ex.targetSets + 1
                                            viewModel.addExerciseToRoutine(activeDetailedRoutine.id, ex.exerciseName, nextSets)
                                            viewModel.removeExerciseFromRoutine(ex) // replace it
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "+1 Serie", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.removeExerciseFromRoutine(ex) }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextSecundario, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showAddExerciseDialogInDetails = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("add_exercise_to_routine_details_button"),
                        border = BorderStroke(1.dp, com.example.ui.theme.BorderColorSubtle),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Añadir Ejercicio", fontSize = 12.sp)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            // Dialog inside detailed Day view to add exercise
            if (showAddExerciseDialogInDetails) {
                var searchInSuperior by remember { mutableStateOf(true) }
                val targetList = if (searchInSuperior) superiorExercises else inferiorExercises
                var tempExerciseName by remember { mutableStateOf("") }
                var tempSetsCount by remember { mutableStateOf("3") }

                AlertDialog(
                    onDismissRequest = { showAddExerciseDialogInDetails = false },
                    shape = RoundedCornerShape(20.dp),
                    title = { Text(text = "Añadir Ejercicio", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Category filter chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (searchInSuperior) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                        .background(if (searchInSuperior) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { searchInSuperior = true }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Superior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, if (!searchInSuperior) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                        .background(if (!searchInSuperior) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { searchInSuperior = false }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Inferior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Vertical exercise list (scrollable up to ~200dp)
                            if (targetList.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                        .liquidGlassModifier(RoundedCornerShape(12.dp))
                                ) {
                                    val listScroll = androidx.compose.foundation.rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .verticalScroll(listScroll)
                                    ) {
                                        targetList.forEachIndexed { index, exObj ->
                                            val isSelected = tempExerciseName == exObj.name
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { tempExerciseName = exObj.name }
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = exObj.name,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) Color.White else TextSecundario,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            if (index < targetList.lastIndex) {
                                                HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "Sin ejercicios en esta categoría. Añádelos desde \"Lista de Ejercicios\".",
                                    fontSize = 11.sp,
                                    color = TextSecundario
                                )
                            }

                            // Manual name override
                            OutlinedTextField(
                                value = tempExerciseName,
                                onValueChange = { tempExerciseName = it },
                                label = { Text("Manual", color = TextSecundario) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    cursorColor = Color.White,
                                    focusedContainerColor = Color(0x05FFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF))
                            )

                            OutlinedTextField(
                                value = tempSetsCount,
                                onValueChange = { tempSetsCount = it },
                                label = { Text("Series objetivo", color = TextSecundario) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                                    cursorColor = Color.White,
                                    focusedContainerColor = Color(0x05FFFFFF),
                                    unfocusedContainerColor = Color(0x05FFFFFF))
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (tempExerciseName.trim().isNotEmpty()) {
                                    val setsValue = tempSetsCount.toIntOrNull() ?: 3
                                    val selectedCat = if (searchInSuperior) "Parte Superior" else "Parte Inferior"
                                    viewModel.addExerciseToRoutine(activeDetailedRoutine.id, tempExerciseName, setsValue, selectedCat)
                                    tempExerciseName = ""
                                }
                            }
                        ) {
                            Text(text = "Añadir", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            tempExerciseName = ""
                            showAddExerciseDialogInDetails = false
                        }) {
                            Text(text = "Aplicar", color = TextSecundario)
                        }
                    },
                    containerColor = Color(0xFA040A18),
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }

    // --- DIALOG: CREATE NEW ROUTINE ---
    if (showCreateRoutineDialog) {
        var tempRoutineName by remember { mutableStateOf("") }
        var tempRoutineDesc by remember { mutableStateOf("") }
        var searchInSuperior by remember { mutableStateOf(true) }
        val targetList = if (searchInSuperior) superiorExercises else inferiorExercises
        val selectedExercises = remember { mutableStateListOf<String>() }

        AlertDialog(
            onDismissRequest = { showCreateRoutineDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(text = "Nueva Rutina", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Name field
                    OutlinedTextField(
                        value = tempRoutineName,
                        onValueChange = { tempRoutineName = it },
                        label = { Text("Nombre de la rutina", color = TextSecundario) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color(0x05FFFFFF))
                    )

                    OutlinedTextField(
                        value = tempRoutineDesc,
                        onValueChange = { tempRoutineDesc = it },
                        label = { Text("Descripción (opcional)", color = TextSecundario) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color(0x05FFFFFF),
                            unfocusedContainerColor = Color(0x05FFFFFF))
                    )

                    // Exercise selector
                    Text(text = "Selecciona ejercicios iniciales:", fontSize = 12.sp, color = TextSecundario, fontWeight = FontWeight.Bold)

                    // Category tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (searchInSuperior) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                .background(if (searchInSuperior) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { searchInSuperior = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Parte Superior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, if (!searchInSuperior) Color.White else BorderColor, RoundedCornerShape(8.dp))
                                .background(if (!searchInSuperior) Color.White.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { searchInSuperior = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Parte Inferior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Scrollable vertical exercise list
                    if (targetList.isEmpty()) {
                        Text(
                            text = "Sin ejercicios. Añádelos primero desde la pestaña \"Lista de Ejercicios\".",
                            fontSize = 11.sp,
                            color = TextSecundario
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .liquidGlassModifier(RoundedCornerShape(12.dp))
                        ) {
                            val listScroll = androidx.compose.foundation.rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .verticalScroll(listScroll)
                            ) {
                                targetList.forEachIndexed { index, exObj ->
                                    val isChecked = selectedExercises.contains(exObj.name)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isChecked) Color.White.copy(alpha = 0.07f) else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isChecked) selectedExercises.remove(exObj.name)
                                                else selectedExercises.add(exObj.name)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = exObj.name,
                                            fontSize = 13.sp,
                                            color = if (isChecked) Color.White else TextSecundario,
                                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    if (index < targetList.lastIndex) {
                                        HorizontalDivider(color = BorderColor, thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    // Summary of selected
                    if (selectedExercises.isNotEmpty()) {
                        Text(
                            text = "${selectedExercises.size} ejercicio(s) seleccionado(s)",
                            fontSize = 11.sp,
                            color = TextSecundario
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempRoutineName.trim().isNotEmpty()) {
                            viewModel.addRoutine(tempRoutineName, tempRoutineDesc, selectedExercises.toList())
                            showCreateRoutineDialog = false
                        }
                    }
                ) {
                    Text(text = "Crear Rutina", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRoutineDialog = false }) {
                    Text(text = "Cancelar", color = TextSecundario)
                }
            },
            containerColor = Color(0xFA040A18),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}

@Composable
fun RoutineRowItem(
    routine: Routine,
    viewModel: FitnessViewModel,
    onClick: () -> Unit,
    onStartWorkout: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val exerciseList by viewModel.getExercisesForRoutine(routine.id).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassModifier(RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
                    contentDescription = "Workout",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = routine.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onStartWorkout,
                    modifier = Modifier
                        .background(Color(0xFF00E676).copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, Color(0xFF00E676), CircleShape)
                        .size(32.dp)
                        .testTag("routine_row_item_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Iniciar Entrenamiento",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Routine", tint = TextSecundario, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.White
                    )
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar rutina", color = Color.White) },
                text = { Text("¿Estás seguro de que quieres eliminar esta rutina?", color = TextSecundario) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }) { Text("Eliminar", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar", color = Color.White) }
                },
                containerColor = Color(0xFA040A18)
            )
        }

        if (routine.description.isNotEmpty()) {
            Text(
                text = routine.description,
                fontSize = 12.sp,
                color = TextSecundario,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Show expandable list of exercises
        if (expanded && exerciseList.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exerciseList.forEach { ex ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(4.dp).background(Color.White, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = ex.exerciseName, fontSize = 13.sp, color = Color.White)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "${ex.targetSets} series", fontSize = 11.sp, color = TextSecundario)
                    }
                }
            }
        }
    }
}
