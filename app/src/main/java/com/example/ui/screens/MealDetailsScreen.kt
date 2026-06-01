package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.database.Food
import com.example.data.database.Meal
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailsScreen(
    category: String,
    dateString: String,
    viewModel: FitnessViewModel,
    onBack: () -> Unit
) {
    var mealDescriptionInput by remember { mutableStateOf("") }
    val meals by viewModel.selectedDateMeals.collectAsState()
    val mealFoodsMap by viewModel.mealFoods.collectAsState()
    val mealAnalysisLoading by viewModel.mealAnalysisLoading.collectAsState()

    BackHandler {
        onBack()
    }

    // Filter meals matching the active category
    val currentCategoryMeals = remember(meals) {
        meals.filter { it.category == category }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = category,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "$dateString",
                    fontSize = 11.sp,
                    color = TextSecundario
                )
            }
        }

        // Language natural text field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF040A18).copy(alpha = 0.96f), RoundedCornerShape(16.dp))
                .liquidGlassModifier(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Registrar mediante Lenguaje Natural",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Describe lo que comiste exactamente. Nuestro Coach de IA calculará las calorías, carbohidratos, proteínas y grasas.",
                fontSize = 10.sp,
                color = TextSecundario,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = mealDescriptionInput,
                onValueChange = { mealDescriptionInput = it },
                placeholder = { Text("Desayuné tazón de avena con 100g de fresas, un plátano entero y scoop de proteína en polvo", fontSize = 11.sp, color = TextSecundario) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("natural_meal_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White,
                    cursorColor = Color.White
                , focusedContainerColor = Color(0x05FFFFFF), unfocusedContainerColor = Color(0x05FFFFFF))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (mealDescriptionInput.trim().isNotEmpty()) {
                        viewModel.logMealFromNaturalLanguage(category, mealDescriptionInput, dateString) { success ->
                            if (success) {
                                mealDescriptionInput = ""
                            }
                        }
                    }
                },
                enabled = !mealAnalysisLoading && mealDescriptionInput.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("log_meal_ai_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (mealAnalysisLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Analizar con IA de SynergyFit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Text(
            text = "Alimentos Registrados",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // List of logged foods
        if (currentCategoryMeals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .liquidGlassModifier(RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay registros hoy para esta categoría.",
                    color = TextSecundario,
                    fontSize = 11.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentCategoryMeals) { meal ->
                    val foods = mealFoodsMap[meal.id] ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlassModifier(RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (meal.inputText.length > 40) "${meal.inputText.take(40)}..." else meal.inputText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total: ${meal.totalCalories} kcal  |  P: ${meal.totalProtein.toInt()}g  |  C: ${meal.totalCarbs.toInt()}g  |  G: ${meal.totalFat.toInt()}g",
                                    fontSize = 11.sp,
                                    color = TextSecundario,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteMeal(meal.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Meal", tint = TextSecundario, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Nested Food list parsed
                        if (foods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BorderColorSubtle)
                            Spacer(modifier = Modifier.height(8.dp))

                            foods.forEach { food ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = food.name,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${food.calories} kcal  (P: ${food.protein.toInt()}g  C: ${food.carbs.toInt()}g)",
                                        fontSize = 10.sp,
                                        color = TextSecundario
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
