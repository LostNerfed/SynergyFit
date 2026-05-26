package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "fit_settings")
data class FitSettings(
    @PrimaryKey val id: Int = 1,
    val username: String = "Usuario",
    val iaProvider: String = "Gemini", // "Gemini", "Groq", "DeepSeek"
    val apiKey: String = "",
    val fitnessGoal: String = "Hipertrofia", // "Hipertrofia", "Pérdida de grasa", etc.
    val bodyWeight: Double = 70.0,
    val targetCalories: Int = 2500,
    val targetProtein: Int = 140,
    val targetCarbs: Int = 300,
    val targetFat: Int = 70,
    val activeSessionId: Int? = null
) : Serializable

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = ""
) : Serializable

@Entity(tableName = "plan_exercises")
data class PlanExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int,
    val exerciseName: String,
    val targetSets: Int = 3,
    val orderIndex: Int = 0
) : Serializable

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int?,
    val routineName: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0
) : Serializable

@Entity(tableName = "session_logs")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val isDropset: Boolean = false,
    val setIndex: Int = 0
) : Serializable

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // "YYYY-MM-DD"
    val category: String, // "Desayuno", "Almuerzo", "Cena", "Snack"
    val inputText: String = "", // natural language description
    val totalCalories: Int = 0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0
) : Serializable

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealId: Int, // links to Meal
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
) : Serializable

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val name: String, // exercise name (unique)
    val category: String = "Otros" // muscle group
) : Serializable
