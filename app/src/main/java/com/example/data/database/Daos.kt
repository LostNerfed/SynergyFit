package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitSettingsDao {
    @Query("SELECT * FROM fit_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<FitSettings?>

    @Query("SELECT * FROM fit_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): FitSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: FitSettings)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY id DESC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Int): Routine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Delete
    suspend fun deleteRoutine(routine: Routine)
}

@Dao
interface PlanExerciseDao {
    @Query("SELECT * FROM plan_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: Int): Flow<List<PlanExercise>>

    @Query("SELECT * FROM plan_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    suspend fun getExercisesForRoutineSync(routineId: Int): List<PlanExercise>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanExercise(exercise: PlanExercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanExercises(exercises: List<PlanExercise>)

    @Delete
    suspend fun deletePlanExercise(exercise: PlanExercise)

    @Query("DELETE FROM plan_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesForRoutine(routineId: Int)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY dateMillis DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Int): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    @Delete
    suspend fun deleteSession(session: Session)
}

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    fun getLogsForSession(sessionId: Int): Flow<List<SessionLog>>

    @Query("SELECT * FROM session_logs WHERE sessionId = :sessionId ORDER BY id ASC")
    suspend fun getLogsForSessionSync(sessionId: Int): List<SessionLog>

    @Query("SELECT * FROM session_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<SessionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionLog(log: SessionLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionLogs(logs: List<SessionLog>)

    @Delete
    suspend fun deleteSessionLog(log: SessionLog)

    @Query("DELETE FROM session_logs WHERE sessionId = :sessionId")
    suspend fun deleteLogsForSession(sessionId: Int)
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE dateString = :dateString")
    fun getMealsForDate(dateString: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals ORDER BY dateString DESC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :id LIMIT 1")
    suspend fun getMealById(id: Int): Meal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal): Long

    @Delete
    suspend fun deleteMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Int)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE mealId = :mealId ORDER BY id ASC")
    fun getFoodsForMeal(mealId: Int): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE mealId = :mealId ORDER BY id ASC")
    suspend fun getFoodsForMealSync(mealId: Int): List<Food>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<Food>)

    @Query("DELETE FROM foods WHERE mealId = :mealId")
    suspend fun deleteFoodsForMeal(mealId: Int)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFoodById(id: Int)
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name ASC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    suspend fun getAllExercisesSync(): List<Exercise>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercise(exercise: Exercise)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExercises(exercises: List<Exercise>)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun getCount(): Int

    @Delete
    suspend fun deleteExercise(exercise: Exercise)
}
