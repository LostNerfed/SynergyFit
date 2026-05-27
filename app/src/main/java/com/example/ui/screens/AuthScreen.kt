package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    onLoginSuccess: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBg)
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
            // Stylized typographic logo
            Text(
                text = "SYNERGY",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "FIT",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

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
                    .testTag("local_name_input"),
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

            AnimatedVisibility(visible = error != null) {
                Text(
                    text = error ?: "",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        error = "Por favor introduce un nombre válido"
                    } else {
                        onLoginSuccess(name.trim())
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
                    .background(com.example.ui.theme.AmoledSurface, RoundedCornerShape(12.dp)).border(1.dp, com.example.ui.theme.PremiumGradientBorder, RoundedCornerShape(12.dp))
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
