package com.example.ui.screens

import com.example.ui.theme.liquidGlassModifier
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.AmoledBg
import com.example.ui.theme.BorderColor
import com.example.ui.theme.TextSecundario

@Composable
fun AuthScreen(
    onLoginSuccess: (String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLbs by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        error = "Por favor introduce un nombre válido"
                    } else {
                        onLoginSuccess(name.trim(), isLbs)
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
