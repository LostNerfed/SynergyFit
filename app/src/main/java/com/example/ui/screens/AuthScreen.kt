package com.example.ui.screens

import com.example.ui.theme.liquidGlassModifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.ui.theme.AmoledBg
import com.example.ui.theme.BorderColor
import com.example.ui.theme.TextSecundario

@Composable
fun AuthScreen(
    onLoginSuccess: (String, Boolean, String, Int, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLbs by remember { mutableStateOf(false) }
    
    var gender by remember { mutableStateOf("Hombre") }
    var ageStr by remember { mutableStateOf("") }
    var heightStr by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Sedentario (0 entrenamientos/sem)") }
    var activityExpanded by remember { mutableStateOf(false) }

    val activityOptions = listOf(
        "Sedentario (0 entrenamientos/sem)",
        "Ligero (1-3 entrenamientos/sem)",
        "Moderado (3-5 entrenamientos/sem)",
        "Activo (6-7 entrenamientos/sem)",
        "Muy Activo (Doble turno o trabajo físico exigente)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Stylized typographic logo in Pointless font (Responsive BoxWithConstraints)
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val screenWidth = maxWidth
                val synergyFontSize = (screenWidth.value * 0.11f).coerceIn(24f, 44f).sp
                val fitFontSize = (synergyFontSize.value * 0.48f).sp

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SYNERGY",
                        fontFamily = com.example.ui.theme.PointlessFontFamily,
                        fontSize = synergyFontSize,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "FIT",
                        fontFamily = com.example.ui.theme.PointlessFontFamily,
                        fontSize = fitFontSize,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    error = null
                },
                placeholder = { Text("Ingresa tu nombre", color = TextSecundario) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassModifier(RoundedCornerShape(12.dp))
                    .testTag("local_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(visible = error != null) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            // Unit Selector
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isLbs) Color.White.copy(alpha = 0.97f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (!isLbs) Color.White.copy(alpha = 0.97f) else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isLbs = false }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kilogramos (kg)", color = if (!isLbs) Color.Black else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isLbs) Color.White.copy(alpha = 0.97f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isLbs) Color.White.copy(alpha = 0.97f) else BorderColor,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { isLbs = true }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Libras (lbs)", color = if (isLbs) Color.Black else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gender selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (gender == "Hombre") Color.White.copy(alpha = 0.97f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .border(1.dp, if (gender == "Hombre") Color.White.copy(alpha = 0.97f) else BorderColor, RoundedCornerShape(8.dp))
                        .clickable { gender = "Hombre" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Hombre", color = if (gender == "Hombre") Color.Black else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (gender == "Mujer") Color.White.copy(alpha = 0.97f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .border(1.dp, if (gender == "Mujer") Color.White.copy(alpha = 0.97f) else BorderColor, RoundedCornerShape(8.dp))
                        .clickable { gender = "Mujer" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Mujer", color = if (gender == "Mujer") Color.Black else Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Age and Height fields
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it.filter { char -> char.isDigit() }.take(3) },
                    placeholder = { Text("Edad (años)", color = TextSecundario, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).liquidGlassModifier(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it.filter { char -> char.isDigit() || char == '.' }.take(5) },
                    placeholder = { Text("Altura (cm)", color = TextSecundario, fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).liquidGlassModifier(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activity level dropdown logic using Box + border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassModifier(RoundedCornerShape(12.dp))
                    .clickable { activityExpanded = !activityExpanded }
                    .padding(16.dp)
            ) {
                Text(text = activityLevel, color = Color.White, fontSize = 13.sp)
                DropdownMenu(
                    expanded = activityExpanded,
                    onDismissRequest = { activityExpanded = false },
                    modifier = Modifier.background(Color(0xFF0D1B2A))
                ) {
                    activityOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, color = Color.White, fontSize = 12.sp) },
                            onClick = {
                                activityLevel = option
                                activityExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val ageInt = ageStr.toIntOrNull() ?: 0
                    val heightDouble = heightStr.toDoubleOrNull() ?: 0.0

                    if (name.trim().isEmpty()) {
                        error = "Por favor introduce un nombre válido"
                    } else if (ageInt <= 0 || ageInt > 120) {
                        error = "Por favor introduce una edad válida"
                    } else if (heightDouble <= 50.0 || heightDouble > 250.0) {
                        error = "Por favor introduce una altura válida (cm)"
                    } else {
                        onLoginSuccess(name.trim(), isLbs, gender, ageInt, heightDouble, activityLevel)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("local_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Ingresar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Terms and Privacy card focusing on offline/local security
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .liquidGlassModifier(RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Términos y Privacidad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Todos tus datos y registros se almacenan exclusivamente de forma local en tu dispositivo para proteger al máximo tu privacidad. No enviamos información de tus rutinas o comidas a servidores externos.",
                        fontSize = 11.sp,
                        color = TextSecundario,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
