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
    version = 3,
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
        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE fit_settings ADD COLUMN gender TEXT NOT NULL DEFAULT 'Hombre'")
                db.execSQL("ALTER TABLE fit_settings ADD COLUMN age INTEGER NOT NULL DEFAULT 25")
                db.execSQL("ALTER TABLE fit_settings ADD COLUMN heightCm REAL NOT NULL DEFAULT 170.0")
                db.execSQL("ALTER TABLE fit_settings ADD COLUMN activityLevel TEXT NOT NULL DEFAULT 'Sedentario'")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "synergyfit_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
