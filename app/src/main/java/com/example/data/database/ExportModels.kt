package com.example.data.database

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExportedRoutine(
    val name: String,
    val description: String,
    val exercises: List<ExportedPlanExercise>
)

@JsonClass(generateAdapter = true)
data class ExportedPlanExercise(
    val exerciseName: String,
    val targetSets: Int,
    val orderIndex: Int
)

@JsonClass(generateAdapter = true)
data class ExportData(
    val version: Int = 1,
    val routines: List<ExportedRoutine>
)
