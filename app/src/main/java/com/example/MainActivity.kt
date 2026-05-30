package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FitnessViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: FitnessViewModel = viewModel()
                val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
                val activeSession by viewModel.activeSession.collectAsState()

                // Custom navigation state
                var currentMainTab by remember { mutableStateOf("home") }
                var activeMealDetailCategory by remember { mutableStateOf<String?>(null) }
                var activeMealDetailDate by remember { mutableStateOf<String?>(null) }
                var isSettingsOpen by remember { mutableStateOf(false) }

                // Check deep link intents for widgets
                LaunchedEffect(intent) {
                    handleDeepLinkIntent(intent) { category ->
                        currentMainTab = "nutrition"
                        activeMealDetailCategory = category
                        activeMealDetailDate = getFormattedToday()
                    }
                }

                val mealCategory = activeMealDetailCategory
                val mealDate = activeMealDetailDate

                // Handle system back button for all navigation states
                BackHandler(enabled = isSettingsOpen) {
                    isSettingsOpen = false
                }
                BackHandler(enabled = mealCategory != null) {
                    activeMealDetailCategory = null
                    activeMealDetailDate = null
                }
                BackHandler(enabled = currentMainTab != "home" && mealCategory == null && !isSettingsOpen && activeSession == null) {
                    currentMainTab = "home"
                }

                SynergyBackground {
                    if (!isUserLoggedIn) {
                        // Registration / Landing
                        AuthScreen(
                            onLoginSuccess = { name ->
                                viewModel.loginLocalUser(name)
                            }
                        )
                    } else if (activeSession != null) {
                        // High-Priority fullscreen tracker
                        ActiveWorkoutScreen(
                            viewModel = viewModel,
                            onFinish = {
                                // returns to plan routines
                                currentMainTab = "plan"
                            }
                        )
                    } else if (isSettingsOpen) {
                        // Fullscreen Settings
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { isSettingsOpen = false }
                        )
                    } else if (mealCategory != null && mealDate != null) {
                        // Fullscreen nutrition log
                        MealDetailsScreen(
                            category = mealCategory,
                            dateString = mealDate,
                            viewModel = viewModel,
                            onBack = {
                                activeMealDetailCategory = null
                                activeMealDetailDate = null
                            }
                        )
                    } else {
                        // Standard view with Bottom Tab Navigation amoled bar
                        Scaffold(
                            containerColor = Color.Transparent,
                            bottomBar = {
                                Box(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 20.dp)
                                        .liquidGlassModifier(RoundedCornerShape(32.dp))
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier.clip(RoundedCornerShape(32.dp)),
                                        windowInsets = WindowInsets(0.dp)
                                    ) {
                                        NavigationBarItem(
                                            selected = currentMainTab == "home",
                                            onClick = { currentMainTab = "home" },
                                            icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Inicio") },
                                            label = { Text("Inicio", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                unselectedIconColor = TextSecundario,
                                                selectedTextColor = Color.White,
                                                unselectedTextColor = TextSecundario,
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentMainTab == "nutrition",
                                            onClick = { currentMainTab = "nutrition" },
                                            icon = { Icon(imageVector = Icons.Default.RestaurantMenu, contentDescription = "Nutrición") },
                                            label = { Text("Nutrición", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                unselectedIconColor = TextSecundario,
                                                selectedTextColor = Color.White,
                                                unselectedTextColor = TextSecundario,
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentMainTab == "plan",
                                            onClick = { currentMainTab = "plan" },
                                            icon = { Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "Rutinas") },
                                            label = { Text("Rutinas", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                unselectedIconColor = TextSecundario,
                                                selectedTextColor = Color.White,
                                                unselectedTextColor = TextSecundario,
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentMainTab == "progress",
                                            onClick = { currentMainTab = "progress" },
                                            icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Progreso") },
                                            label = { Text("Progreso", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                unselectedIconColor = TextSecundario,
                                                selectedTextColor = Color.White,
                                                unselectedTextColor = TextSecundario,
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                        NavigationBarItem(
                                            selected = currentMainTab == "calendar",
                                            onClick = { currentMainTab = "calendar" },
                                            icon = { Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Historial") },
                                            label = { Text("Historial", fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                unselectedIconColor = TextSecundario,
                                                selectedTextColor = Color.White,
                                                unselectedTextColor = TextSecundario,
                                                indicatorColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentMainTab) {
                                    "home" -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToSettings = { isSettingsOpen = true },
                                        onStartActiveWorkout = { routine ->
                                            viewModel.startActiveWorkout(routine)
                                        },
                                        onStartCustomWorkout = {
                                            viewModel.startCustomActiveWorkout()
                                        }
                                    )
                                    "nutrition" -> NutritionHomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToMealDetails = { category, date ->
                                            activeMealDetailCategory = category
                                            activeMealDetailDate = date
                                        }
                                    )
                                    "plan" -> PlanDaysScreen(
                                        viewModel = viewModel,
                                        onStartRoutineWorkout = { routine ->
                                            viewModel.startActiveWorkout(routine)
                                        }
                                    )
                                    "progress" -> ProgressScreen(
                                        viewModel = viewModel
                                    )
                                    "calendar" -> CalendarScreen(
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent?, onLaunchMealDetails: (String) -> Unit) {
        if (intent == null) return
        val action = intent.action
        val data: Uri? = intent.data
        if (Intent.ACTION_VIEW == action && data != null) {
            // Check link scheme: synergyfit://add-meal?category=X
            if ("synergyfit" == data.scheme && "add-meal" == data.host) {
                val categoryParam = data.getQueryParameter("category")
                if (categoryParam != null) {
                    Log.d(TAG, "Navigating deep link to category: $categoryParam")
                    onLaunchMealDetails(categoryParam)
                }
            }
        }
    }
}
