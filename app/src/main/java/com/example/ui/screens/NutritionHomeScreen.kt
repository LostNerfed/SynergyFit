package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Meal
import com.example.ui.FitnessViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NutritionHomeScreen(
    viewModel: FitnessViewModel,
    onNavigateToMealDetails: (category: String, date: String) -> Unit
) {
    val settings by viewModel.settingsState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val meals by viewModel.selectedDateMeals.collectAsState()
    val weeklyCaloriesMap by viewModel.weeklyCaloriesState.collectAsState()

    val dailyInsight by viewModel.dailyInsight.collectAsState()
    val dailyInsightLoading by viewModel.dailyInsightLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    // Calculate sum of consumed macros for the day
    val totalCalories = meals.sumOf { it.totalCalories }
    val totalProtein = meals.sumOf { it.totalProtein }
    val totalCarbs = meals.sumOf { it.totalCarbs }
    val totalFat = meals.sumOf { it.totalFat }

    // Macros fractions
    val proteinFraction = if (settings.targetProtein > 0) (totalProtein / settings.targetProtein).toFloat().coerceIn(0f, 1f) else 0f
    val carbsFraction = if (settings.targetCarbs > 0) (totalCarbs / settings.targetCarbs).toFloat().coerceIn(0f, 1f) else 0f
    val fatFraction = if (settings.targetFat > 0) (totalFat / settings.targetFat).toFloat().coerceIn(0f, 1f) else 0f

    // Categorized calories
    val mealCategories = listOf(
        CategoryData("Desayuno", Icons.Default.LightMode, meals.filter { it.category == "Desayuno" }),
        CategoryData("Almuerzo", Icons.Default.WbSunny, meals.filter { it.category == "Almuerzo" }),
        CategoryData("Cena", Icons.Default.NightlightRound, meals.filter { it.category == "Cena" }),
        CategoryData("Snack", Icons.Default.RestaurantMenu, meals.filter { it.category == "Snack" })
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBg)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Upper buffer margin
        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Date Picker Horizontal
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Diario Nutricional",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Reset to today shortcut
                    IconButton(
                        onClick = { viewModel.selectDate(getFormattedToday()) },
                        modifier = Modifier
                            .border(1.dp, BorderColor, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDatePicker(
                    selectedDate = selectedDate,
                    onDateSelect = { dateStr -> viewModel.selectDate(dateStr) }
                )
            }
        }

        // Summary of macro targets
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Consumido hoy", fontSize = 12.sp, color = TextSecundario)
                        Text(
                            text = "$totalCalories kcal",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Objetivo diario", fontSize = 12.sp, color = TextSecundario)
                        Text(
                            text = "${settings.targetCalories} kcal",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress line
                LinearProgressIndicator(
                    progress = { if (settings.targetCalories > 0) (totalCalories.toFloat() / settings.targetCalories).coerceIn(0f, 1f) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color.White,
                    trackColor = BorderColorSubtle,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Detail of Proteínas, Carbohidratos y Grasas linear progress bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MacroProgressWidget(
                        title = "Proteínas",
                        amount = "${totalProtein.toInt()}g",
                        target = "${settings.targetProtein}g",
                        ratio = proteinFraction,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressWidget(
                        title = "Carbos",
                        amount = "${totalCarbs.toInt()}g",
                        target = "${settings.targetCarbs}g",
                        ratio = carbsFraction,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressWidget(
                        title = "Grasas",
                        amount = "${totalFat.toInt()}g",
                        target = "${settings.targetFat}g",
                        ratio = fatFraction,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // IA Nutritive Insight Card Widget
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .background(BorderColorSubtle)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Consejo Nutricional de la IA",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (dailyInsightLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterHorizontally),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (dailyInsight.trim().isNotEmpty()) dailyInsight else "Analizando tus comidas para sugerirte mejoras...",
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Categories List (Desayuno, Almuerzo, Cena, Snack)
        item {
            Text(
                text = "Comidas del día",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        items(mealCategories) { item ->
            val sumCal = item.meals.sumOf { it.totalCalories }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToMealDetails(item.category, selectedDate) }
                    .padding(16.dp),
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
                            imageVector = item.icon,
                            contentDescription = item.category,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        val totalFoods = item.meals.size
                        Text(
                            text = if (totalFoods == 0) "Sin alimentos aún" else "$totalFoods alimentos registrados",
                            color = TextSecundario,
                            fontSize = 11.sp
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$sumCal kcal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = TextSecundario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Minimal Weekly Bar Chart
        item {
            Text(
                text = "Consumo Semanal (Últimos 7 días)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 10.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            WeeklyNutritionBarChart(weeklyCaloriesMap)
        }

        // Bottom padding
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun MacroProgressWidget(
    title: String,
    amount: String,
    target: String,
    ratio: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(1.dp, BorderColorSubtle, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 11.sp, color = TextSecundario, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = amount, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = "de $target", fontSize = 10.sp, color = TextSecundario)
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color.White,
            trackColor = BorderColorSubtle,
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun HorizontalDatePicker(
    selectedDate: String,
    onDateSelect: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val dates = remember { generateDatesAroundToday() }

    // Scroll to position matching selected dates
    LaunchedEffect(selectedDate) {
        val idx = dates.indexOfFirst { it.dateString == selectedDate }
        if (idx != -1) {
            listState.animateScrollToItem((idx - 2).coerceAtLeast(0))
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().testTag("horizontal_date_picker"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dates) { item ->
            val isSelected = item.dateString == selectedDate
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(68.dp)
                    .background(
                        if (isSelected) Color.White else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.White else BorderColor,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onDateSelect(item.dateString) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = item.dayOfWeek,
                        color = if (isSelected) Color.Black else TextSecundario,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.dayNumber,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Custom amoled weekly bar chart
@Composable
fun WeeklyNutritionBarChart(weeklyCaloriesMap: Map<String, Int>) {
    val dates = weeklyCaloriesMap.keys.sorted().takeLast(7)
    val values = dates.map { weeklyCaloriesMap[it] ?: 0 }
    val maxValue = values.maxOrNull()?.coerceAtLeast(2000) ?: 2000

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        dates.forEachIndexed { i, dateString ->
            val calories = weeklyCaloriesMap[dateString] ?: 0
            val fraction = calories.toFloat() / maxValue
            val heightPercent = fraction.coerceIn(0.05f, 1f)

            val displayDay = getShortDayName(dateString)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (calories > 0) "${calories}" else "-",
                    fontSize = 8.sp,
                    color = if (calories > 0) Color.White else TextSecundario,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // The bar
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(70.dp * heightPercent)
                ) {
                    drawRect(
                        color = if (calories > 0) Color.White else BorderColorSubtle,
                        size = size
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = displayDay,
                    fontSize = 10.sp,
                    color = TextSecundario,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helpers
data class DateItem(
    val dateString: String,
    val dayOfWeek: String,
    val dayNumber: String
)

data class CategoryData(
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val meals: List<Meal>
)

fun generateDatesAroundToday(): List<DateItem> {
    val list = mutableListOf<DateItem>()
    val cal = Calendar.getInstance()
    // Go -15 days back and +15 days forward
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US) // Standard for databases, but keep safe
    val dayNumFormat = SimpleDateFormat("d", Locale("es", "ES"))
    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("es", "ES")) // "lun", "mar"

    cal.add(Calendar.DAY_OF_YEAR, -15)
    for (i in 0 until 31) {
        list.add(
            DateItem(
                dateString = format.format(cal.time),
                dayOfWeek = dayOfWeekFormat.format(cal.time).uppercase().take(3),
                dayNumber = dayNumFormat.format(cal.time)
            )
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}

fun getFormattedToday(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

fun getShortDayName(dateString: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString) ?: Date()
        val format = SimpleDateFormat("EE", Locale("es", "ES")) // "Lu", "Ma", "Mi"
        format.format(date).take(3).uppercase()
    } catch (e: Exception) {
        ""
    }
}
