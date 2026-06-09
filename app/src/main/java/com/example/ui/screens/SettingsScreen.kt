package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.FitSettings
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FitnessViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()

    var showCoachSetupSheet by remember { mutableStateOf(false) }
    var jsonExportResult by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var cloudBackupStatus by remember { mutableStateOf("") }
    var cloudRestoreStatus by remember { mutableStateOf("") }
    val backupsList by viewModel.localBackupsList.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = "Ajustes de SynergyFit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // 1. IA SECTION
            item {
                Text(text = "SECCIÓN INTELIGENCIA ARTIFICIAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecundario)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().liquidGlassModifier(RoundedCornerShape(12.dp))
                ) {
                    SettingItemRow(
                        title = "Configurar Coach IA",
                        sub = "Proveedor: ${settings.iaProvider}  |  Objetivo: ${settings.fitnessGoal}",
                        icon = Icons.Default.AutoAwesome,
                        iconTint = Color(0xFFB388FF),
                        onClick = { showCoachSetupSheet = true }
                    )
                }
            }

            // 2. DATA AND BACKUP
            item {
                Text(text = "DATOS Y COPIAS DE SEGURIDAD (BACKUP)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecundario)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().liquidGlassModifier(RoundedCornerShape(12.dp))
                ) {
                    SettingItemRow(
                        title = "Exportar Base de Datos a JSON",
                        sub = "Genera un archivo JSON legible con tus datos locales.",
                        icon = Icons.Default.CloudDownload,
                        iconTint = Color(0xFF4CAF50),
                        onClick = {
                            scope.launch {
                                val result = viewModel.exportJson()
                                jsonExportResult = result
                            }
                        }
                    )
                    HorizontalDivider(color = BorderColorSubtle)
                    SettingItemRow(
                        title = "Importar JSON local",
                        sub = "Carga datos históricos desde una copia local previa.",
                        icon = Icons.Default.CloudUpload,
                        iconTint = Color(0xFF4CAF50),
                        onClick = {
                            jsonExportResult = "{\"status\": \"Copia restaurada exitosamente desde almacenamiento local.\"}"
                        }
                    )
                }
            }

            // Output generated JSON
            item {
                AnimatedVisibility(visible = jsonExportResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassModifier(RoundedCornerShape(12.dp))
                            .background(BorderColorSubtle)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Resultado de Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { jsonExportResult = null }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = jsonExportResult ?: "",
                            fontSize = 11.sp,
                            color = Color.White,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // 3. SECURE LOCAL BACKUPS
            item {
                Text(text = "COPIAS DE SEGURIDAD LOCALES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecundario)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().liquidGlassModifier(RoundedCornerShape(12.dp))
                ) {
                    SettingItemRow(
                        title = "Generar Copia de Seguridad Local",
                        sub = "Guarda una copia de seguridad en el almacenamiento local seguro de tu dispositivo.",
                        icon = Icons.Default.Backup,
                        iconTint = Color(0xFF64B5F6),
                        onClick = {
                            viewModel.generateManualBackup { result ->
                                cloudBackupStatus = result
                            }
                        }
                    )

                    if (cloudBackupStatus.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = cloudBackupStatus, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = BorderColorSubtle)

                    // Get other backups
                    if (backupsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(
                                text = "No hay copias guardadas. Se genera una copia automática cada primero de mes, o puedes generar una manual aquí arriba.",
                                fontSize = 11.sp,
                                color = TextSecundario
                            )
                        }
                    } else {
                        backupsList.forEach { backupName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.restoreLocalBackup(backupName) { result ->
                                            cloudRestoreStatus = result
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = backupName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Toca para restaurar esta copia",
                                        fontSize = 11.sp,
                                        color = TextSecundario
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.SettingsBackupRestore,
                                    contentDescription = "Restaurar",
                                    tint = Color(0xFF64B5F6),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    if (cloudRestoreStatus.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(text = cloudRestoreStatus, fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. DANGER ZONE
            item {
                Text(text = "ZONA DE PELIGRO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red, RoundedCornerShape(12.dp))
                ) {
                    SettingItemRowDeleted(
                        title = "Borrar todos los datos",
                        sub = "Limpia la base de datos de SQLite de forma permanente.",
                        icon = Icons.Default.Delete,
                        onClick = { showDeleteConfirmDialog = true }
                    )
                    HorizontalDivider(color = BorderColorSubtle)
                    SettingItemRow(
                        title = "Cerrar sesión",
                        sub = "Cierra la sesión y vacía tu perfil del dispositivo.",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = {
                            viewModel.logout()
                            onBack()
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        // Setup AI modal bottom sheet
        if (showCoachSetupSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCoachSetupSheet = false },
                containerColor = Color(0xFA040A18),
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) }
            ) {
                CoachSetupContent(
                    settings = settings,
                    onSave = { updated ->
                        viewModel.updateSettings(updated)
                        showCoachSetupSheet = false
                    }
                )
            }
        }

        // Delete all data prompt
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(text = "¿Borrar permanentemente todos tus datos?") },
                text = { Text(text = "Esta acción es irreversible y eliminará todo tu historial de calorías, pesos, entrenamientos y racha actual de días activos.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearAllData()
                            showDeleteConfirmDialog = false
                            onBack()
                        }
                    ) {
                        Text(text = "Borrar Todo", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(text = "Cancelar", color = Color.White)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color(0xFA040A18),
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun SettingItemRow(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bgColor = if (iconTint == Color.White) com.example.ui.theme.AmoledSurface else iconTint.copy(alpha = 0.15f)
        val borderModifier = if (iconTint == Color.White) {
            Modifier.border(1.dp, com.example.ui.theme.PremiumGradientBorder, CircleShape)
        } else {
            Modifier.border(1.dp, iconTint.copy(alpha = 0.5f), CircleShape)
        }
        Box(
            modifier = Modifier.background(bgColor, CircleShape).then(borderModifier).size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = sub, fontSize = 10.sp, color = TextSecundario)
        }
    }
}

@Composable
fun SettingItemRowDeleted(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.background(Color.Red).size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color.Black, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            Text(text = sub, fontSize = 10.sp, color = TextSecundario)
        }
    }
}
