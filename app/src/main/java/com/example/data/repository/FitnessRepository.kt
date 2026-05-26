package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FitnessRepository(private val db: AppDatabase) {
    val settings: Flow<FitSettings?> = db.fitSettingsDao().getSettings()

    suspend fun getSettingsSync(): FitSettings {
        return db.fitSettingsDao().getSettingsSync() ?: FitSettings().also {
            db.fitSettingsDao().insertSettings(it)
        }
    }

    suspend fun saveSettings(settings: FitSettings) {
        db.fitSettingsDao().insertSettings(settings)
    }

    // Routines
    val routines: Flow<List<Routine>> = db.routineDao().getAllRoutines()

    suspend fun getRoutineById(id: Int): Routine? = db.routineDao().getRoutineById(id)

    suspend fun insertRoutine(routine: Routine): Long = db.routineDao().insertRoutine(routine)

    suspend fun deleteRoutine(routine: Routine) {
        db.planExerciseDao().deleteExercisesForRoutine(routine.id)
        db.routineDao().deleteRoutine(routine)
    }

    // Plan Exercises
    fun getExercisesForRoutine(routineId: Int): Flow<List<PlanExercise>> =
        db.planExerciseDao().getExercisesForRoutine(routineId)

    suspend fun getExercisesForRoutineSync(routineId: Int): List<PlanExercise> =
        db.planExerciseDao().getExercisesForRoutineSync(routineId)

    suspend fun insertPlanExercise(exercise: PlanExercise) =
        db.planExerciseDao().insertPlanExercise(exercise)

    suspend fun insertPlanExercises(exercises: List<PlanExercise>) =
        db.planExerciseDao().insertPlanExercises(exercises)

    suspend fun deletePlanExercise(exercise: PlanExercise) =
        db.planExerciseDao().deletePlanExercise(exercise)

    suspend fun deleteExercisesForRoutine(routineId: Int) =
        db.planExerciseDao().deleteExercisesForRoutine(routineId)

    // Sessions
    val sessions: Flow<List<Session>> = db.sessionDao().getAllSessions()

    suspend fun getSessionById(id: Int): Session? = db.sessionDao().getSessionById(id)

    suspend fun insertSession(session: Session): Long = db.sessionDao().insertSession(session)

    suspend fun deleteSession(session: Session) {
        db.sessionLogDao().deleteLogsForSession(session.id)
        db.sessionDao().deleteSession(session)
    }

    // Session Logs
    fun getLogsForSession(sessionId: Int): Flow<List<SessionLog>> =
        db.sessionLogDao().getLogsForSession(sessionId)

    suspend fun getLogsForSessionSync(sessionId: Int): List<SessionLog> =
        db.sessionLogDao().getLogsForSessionSync(sessionId)

    val allLogs: Flow<List<SessionLog>> = db.sessionLogDao().getAllLogs()

    suspend fun insertSessionLog(log: SessionLog) = db.sessionLogDao().insertSessionLog(log)

    suspend fun insertSessionLogs(logs: List<SessionLog>) = db.sessionLogDao().insertSessionLogs(logs)

    suspend fun deleteSessionLog(log: SessionLog) = db.sessionLogDao().deleteSessionLog(log)

    // Meals & Foods
    fun getMealsForDate(dateString: String): Flow<List<Meal>> = db.mealDao().getMealsForDate(dateString)

    val allMeals: Flow<List<Meal>> = db.mealDao().getAllMeals()

    suspend fun getMealById(id: Int): Meal? = db.mealDao().getMealById(id)

    suspend fun insertMeal(meal: Meal): Long = db.mealDao().insertMeal(meal)

    suspend fun deleteMealById(id: Int) {
        db.foodDao().deleteFoodsForMeal(id)
        db.mealDao().deleteMealById(id)
    }

    fun getFoodsForMeal(mealId: Int): Flow<List<Food>> = db.foodDao().getFoodsForMeal(mealId)

    suspend fun getFoodsForMealSync(mealId: Int): List<Food> = db.foodDao().getFoodsForMealSync(mealId)

    suspend fun insertFood(food: Food) = db.foodDao().insertFood(food)

    suspend fun insertFoods(foods: List<Food>) = db.foodDao().insertFoods(foods)

    suspend fun deleteFoodById(id: Int) = db.foodDao().deleteFoodById(id)

    // Exercises global registry
    val allExercises: Flow<List<Exercise>> = db.exerciseDao().getAllExercises()

    suspend fun getAllExercisesSync(): List<Exercise> = db.exerciseDao().getAllExercisesSync()

    suspend fun insertExercise(exercise: Exercise) = db.exerciseDao().insertExercise(exercise)

    suspend fun insertExercises(exercises: List<Exercise>) = db.exerciseDao().insertExercises(exercises)

    suspend fun getExercisesCount(): Int = db.exerciseDao().getCount()

    suspend fun deleteExercise(exercise: Exercise) = db.exerciseDao().deleteExercise(exercise)

    // Clear all
    suspend fun clearAllData() {
        db.clearAllTables()
        // Rebuild initial settings
        db.fitSettingsDao().insertSettings(FitSettings())
    }

    // Support simple dependency injection
    companion object {
        @Volatile
        private var INSTANCE: FitnessRepository? = null

        fun getInstance(context: android.content.Context): FitnessRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = FitnessRepository(db)
                INSTANCE = instance
                instance
            }
        }
    }
}
