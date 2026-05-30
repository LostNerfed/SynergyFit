package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FitSettings::class,
        Routine::class,
        PlanExercise::class,
        Session::class,
        SessionLog::class,
        Meal::class,
        Food::class,
        Exercise::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fitSettingsDao(): FitSettingsDao
    abstract fun routineDao(): RoutineDao
    abstract fun planExerciseDao(): PlanExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun sessionLogDao(): SessionLogDao
    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "synergyfit_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
