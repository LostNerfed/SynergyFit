package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Sub-UI state routing
    var selectedRoutineForModelDetails by remember { mutableStateOf<Routine?>(null) }
    var showCreateRoutineDialog by remember { mutableStateOf(false) }
    var activePlanTab by remember { mutableStateOf(0) } // 0 = Tutinas, 1 = Lista Madre de Ejercicios
    var showAddCustomExerciseDialog by remember { mutableStateOf(false) }

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
            .background(AmoledBg)
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
                    .background(BorderColorSubtle, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activePlanTab == 0) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { activePlanTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mis Rutinas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activePlanTab == 0) Color.Black else Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (activePlanTab == 1) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { activePlanTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lista de Ejercicios",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activePlanTab == 1) Color.Black else Color.White
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
                    Column {
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

                    IconButton(
                        onClick = { showCreateRoutineDialog = true },
                        modifier = Modifier
                            .border(1.dp, BorderColor, CircleShape)
                            .size(36.dp)
                            .testTag("add_routine_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Routine", tint = Color.White)
                    }
                }

                if (routines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
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
                            .border(1.dp, BorderColor, CircleShape)
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
                                    .border(1.dp, BorderColorSubtle, RoundedCornerShape(10.dp))
                                    .background(AmoledSurface)
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
                            Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                                    .border(1.dp, BorderColorSubtle, RoundedCornerShape(10.dp))
                                    .background(AmoledSurface)
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
                    title = { Text(text = "Nuevo Ejercicio", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = "Añade un ejercicio a tu lista general de entrenamiento para seleccionarlo luego.", fontSize = 11.sp, color = TextSecundario)
                            
                            OutlinedTextField(
                                value = newExName,
                                onValueChange = { newExName = it },
                                label = { Text("Nombre del Ejercicio", color = TextSecundario) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = Color.White
                                )
                            )

                            Text(text = "Clasificación/Grupo:", fontSize = 11.sp, color = TextSecundario, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp, 
                                            if (isSuperiorSelected) Color.White else BorderColor, 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(if (isSuperiorSelected) BorderColorSubtle else Color.Transparent)
                                        .clickable { isSuperiorSelected = true }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Superior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            1.dp, 
                                            if (!isSuperiorSelected) Color.White else BorderColor, 
                                            RoundedCornerShape(8.dp)
                                        )
                                        .background(if (!isSuperiorSelected) BorderColorSubtle else Color.Transparent)
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
                    containerColor = AmoledSurface,
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
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
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
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
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
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
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
                    title = { Text(text = "Añadir Ejercicio", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "Selecciona de la lista o escribe de manera manual abajo:", fontSize = 11.sp, color = TextSecundario)
                            
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
                                        .clickable { searchInSuperior = true }
                                        .padding(vertical = 8.dp),
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
                                        .clickable { searchInSuperior = false }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Parte Inferior", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Horizontal Quick Select of Exercises
                            if (targetList.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "Tocar para seleccionar:", fontSize = 11.sp, color = TextSecundario)
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(targetList) { exObj ->
                                            Box(
                                                modifier = Modifier
                                                    .background(BorderColorSubtle, RoundedCornerShape(8.dp))
                                                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                                    .clickable { tempExerciseName = exObj.name }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(text = exObj.name, fontSize = 11.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(text = "(Lista de ejercicios vacía en esta categoría)", fontSize = 10.sp, color = TextSecundario)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = tempExerciseName,
                                onValueChange = { tempExerciseName = it },
                                label = { Text("Nombre del Ejercicio (Manual o Seleccionado)", color = TextSecundario) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = tempSetsCount,
                                onValueChange = { tempSetsCount = it },
                                label = { Text("Series Objetivo", color = TextSecundario) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = BorderColor,
                                    cursorColor = Color.White
                                )
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
                                     showAddExerciseDialogInDetails = false
                                 }
                             }
                        ) {
                            Text(text = "Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                tempExerciseName = ""
                                showAddExerciseDialogInDetails = false
                            }
                        ) {
                            Text(text = "Cancelar", color = TextSecundario)
                        }
                    },
                    containerColor = AmoledSurface,
                    titleContentColor = Color.White,
                    textContentColor = Color.White
                )
            }
        }
    }

    // --- DIALOG: CREATE NEW ROUTINE Day Template ---
    if (showCreateRoutineDialog) {
        var tempRoutineName by remember { mutableStateOf("") }
        var tempRoutineDesc by remember { mutableStateOf("") }
        var tempExerciseInputListText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateRoutineDialog = false },
            title = { Text(text = "Nueva Plantilla de Rutina") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Nombre:", fontSize = 12.sp, color = TextSecundario)
                    OutlinedTextField(
                        value = tempRoutineName,
                        onValueChange = { tempRoutineName = it },
                        placeholder = { Text("Nombre de la rutina") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Color.White
                        )
                    )

                    Text(text = "Descripción corta:", fontSize = 12.sp, color = TextSecundario)
                    OutlinedTextField(
                        value = tempRoutineDesc,
                        onValueChange = { tempRoutineDesc = it },
                        placeholder = { Text("Enfoque hombros y espalda") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Color.White
                        )
                    )

                    Text(text = "Añadir ejercicios iniciales (uno por línea):", fontSize = 12.sp, color = TextSecundario)
                    OutlinedTextField(
                        value = tempExerciseInputListText,
                        onValueChange = { tempExerciseInputListText = it },
                        placeholder = { Text("Ejercicio") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempRoutineName.trim().isNotEmpty()) {
                            val lines = tempExerciseInputListText.split("\n")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            viewModel.addRoutine(tempRoutineName, tempRoutineDesc, lines)

                            tempRoutineName = ""
                            tempRoutineDesc = ""
                            tempExerciseInputListText = ""
                            showCreateRoutineDialog = false
                        }
                    }
                ) {
                    Text(text = "Crear Plantilla", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tempRoutineName = ""
                        tempRoutineDesc = ""
                        tempExerciseInputListText = ""
                        showCreateRoutineDialog = false
                    }
                ) {
                    Text(text = "Cancelar", color = TextSecundario)
                }
            },
            containerColor = AmoledSurface,
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
    val exerciseList by viewModel.getExercisesForRoutine(routine.id).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
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
                        .border(1.dp, Color.White, CircleShape)
                        .size(32.dp)
                        .testTag("routine_row_item_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Iniciar Entrenamiento",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Routine", tint = TextSecundario, modifier = Modifier.size(16.dp))
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit", tint = Color.White)
            }
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

        // Show quick summary list of exercises
        if (exerciseList.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                exerciseList.take(3).forEach { ex ->
                    Box(
                        modifier = Modifier
                            .background(BorderColorSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = ex.exerciseName, fontSize = 10.sp, color = Color.White)
                    }
                }
                if (exerciseList.size > 3) {
                    Box(
                        modifier = Modifier
                            .background(BorderColorSubtle, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+${exerciseList.size - 3}", fontSize = 10.sp, color = TextSecundario)
                    }
                }
            }
        }
    }
}
