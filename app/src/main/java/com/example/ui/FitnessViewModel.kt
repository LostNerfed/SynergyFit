package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.database.*
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class FitnessViewModel(private val app: Application) : AndroidViewModel(app) {
    private val repository = FitnessRepository.getInstance(app)
    private val TAG = "FitnessViewModel"
    private val dbWriteMutex = Mutex()

    // Settings
    val settingsState: StateFlow<FitSettings> = repository.settings
        .map { it ?: FitSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FitSettings())

    // Chat FIFO History
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    // Nutrition State
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Observed Meals
    val selectedDateMeals: StateFlow<List<Meal>> = _selectedDate
        .flatMapLatest { date -> repository.getMealsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly summary of calories
    val weeklyCaloriesState: StateFlow<Map<String, Int>> = repository.allMeals
        .map { meals ->
            val calendar = Calendar.getInstance()
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val resultMap = mutableMapOf<String, Int>()

            // Initialize last 7 days with 0 calories
            for (i in 0 until 7) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = format.format(cal.time)
                resultMap[dateStr] = 0
            }

            meals.forEach { meal ->
                if (resultMap.containsKey(meal.dateString)) {
                    resultMap[meal.dateString] = (resultMap[meal.dateString] ?: 0) + meal.totalCalories
                }
            }
            resultMap
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Observed Foods map by mealId
    private val _mealFoods = MutableStateFlow<Map<Int, List<Food>>>(emptyMap())
    val mealFoods: StateFlow<Map<Int, List<Food>>> = _mealFoods.asStateFlow()

    // Active Workout Screen State
    private val _activeSession = MutableStateFlow<Session?>(null)
    val activeSession: StateFlow<Session?> = _activeSession.asStateFlow()

    private val _activeLogs = MutableStateFlow<List<SessionLog>>(emptyList())
    val activeLogs: StateFlow<List<SessionLog>> = _activeLogs.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private var cronometerJob: Job? = null

    // Routines & Plans
    val routines: StateFlow<List<Routine>> = repository.routines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sessions & Historical Logs for Analytics & Calendar
    val sessions: StateFlow<List<Session>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<SessionLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Global Exercises registry state
    val allExercises: StateFlow<List<Exercise>> = repository.allExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Daily Nutrition Insight
    private val _dailyInsight = MutableStateFlow<String>("")
    val dailyInsight: StateFlow<String> = _dailyInsight.asStateFlow()

    private val _dailyInsightLoading = MutableStateFlow(false)
    val dailyInsightLoading: StateFlow<Boolean> = _dailyInsightLoading.asStateFlow()

    // Auth screen profile/local loading
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // Local backup management
    private val _localBackupsList = MutableStateFlow<List<String>>(emptyList())
    val localBackupsList: StateFlow<List<String>> = _localBackupsList.asStateFlow()

    init {
        // Build initial settings record if none exists
        viewModelScope.launch {
            val s = repository.getSettingsSync() ?: FitSettings()
            Log.d(TAG, "Loaded FitSettings: $s")
            // If they already entered username, skip auth screen or proceed
            _isUserLoggedIn.value = s.username != "Usuario" && s.username.trim().isNotEmpty()

            // Delete any existing default preloaded exercises from the database
            val existingEx = repository.getAllExercisesSync()
            val targetsToDelete = setOf("ejercicio", "ejercicio de ejemplo", "press de banca", "dominadas", "press de hombro")
            existingEx.forEach { ex ->
                if (targetsToDelete.contains(ex.name.lowercase().trim())) {
                    repository.deleteExercise(ex)
                }
            }

            // Restore/Auto-save memory recovery: check if a workout remained open/unfinished
            val activeId = s.activeSessionId
            if (activeId != null) {
                val session = repository.getSessionById(activeId)
                if (session != null) {
                    _activeSession.value = session
                    _activeLogs.value = repository.getLogsForSessionSync(activeId)
                    // Estimate elapsed seconds dynamically with a sane 2-hour cap
                    val elapsed = ((System.currentTimeMillis() - session.dateMillis) / 1000).toInt().coerceIn(0, 7200)
                    _elapsedSeconds.value = elapsed
                    startCronometer()
                } else {
                    repository.saveSettings(s.copy(activeSessionId = null))
                }
            }
        }

        // Keep meals and foods updated
        viewModelScope.launch {
            selectedDateMeals.collect { meals ->
                val foodMap = mutableMapOf<Int, List<Food>>()
                meals.forEach { meal ->
                    val foods = repository.getFoodsForMealSync(meal.id)
                    foodMap[meal.id] = foods
                }
                _mealFoods.value = foodMap
            }
        }

        // Automatic local backup on the 1st of each month
        checkAndPerformMonthlyBackup()
        loadLocalBackups()
    }

    // Auth and settings helper
    fun loginLocalUser(name: String) {
        viewModelScope.launch {
            val current = settingsState.value
            val updated = current.copy(username = name)
            repository.saveSettings(updated)
            _isUserLoggedIn.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            val default = FitSettings()
            repository.saveSettings(default)
            _isUserLoggedIn.value = false
            _chatHistory.value = emptyList()
        }
    }

    fun updateSettings(settings: FitSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }

    // Date navigation
    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun selectDateOffset(days: Int) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = format.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = format.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = getTodayDateString()
        }
    }

    // Room operations
    fun getExercisesForRoutine(routineId: Int): Flow<List<PlanExercise>> {
        return repository.getExercisesForRoutine(routineId)
    }

    fun addRoutine(name: String, description: String, exercisesList: List<String>) {
        viewModelScope.launch {
            val routineId = repository.insertRoutine(Routine(name = name, description = description))
            val planExercises = exercisesList.mapIndexed { idx, exName ->
                val capitalized = exName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                // Store in global registry
                repository.insertExercise(Exercise(name = capitalized, category = "Otros"))
                PlanExercise(routineId = routineId.toInt(), exerciseName = capitalized, targetSets = 3, orderIndex = idx)
            }
            repository.insertPlanExercises(planExercises)
        }
    }

    fun addExerciseToRoutine(routineId: Int, name: String, targetSets: Int, category: String = "Otros") {
        viewModelScope.launch {
            val capitalized = name.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            // Store/ensure exist in global registry
            repository.insertExercise(Exercise(name = capitalized, category = category))

            val existing = repository.getExercisesForRoutineSync(routineId)
            val newIdx = existing.size
            repository.insertPlanExercise(
                PlanExercise(routineId = routineId, exerciseName = capitalized, targetSets = targetSets, orderIndex = newIdx)
            )
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    fun removeExerciseFromRoutine(exercise: PlanExercise) {
        viewModelScope.launch {
            repository.deletePlanExercise(exercise)
        }
    }

    fun addCustomExercise(name: String, category: String) {
        val capitalized = name.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        viewModelScope.launch {
            repository.insertExercise(Exercise(name = capitalized, category = category))
        }
    }

    fun deleteCustomExercise(exercise: Exercise) {
        viewModelScope.launch {
            repository.deleteExercise(exercise)
        }
    }

    // Nutrition Meal Details Parsing via Gemini
    private val _mealAnalysisLoading = MutableStateFlow(false)
    val mealAnalysisLoading: StateFlow<Boolean> = _mealAnalysisLoading.asStateFlow()

    fun logMealFromNaturalLanguage(category: String, inputText: String, dateStr: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _mealAnalysisLoading.value = true
            try {
                val config = settingsState.value
                val result = GeminiService.analyzeMeal(inputText, config)
                if (result != null) {
                    val mealId = repository.insertMeal(
                        Meal(
                            dateString = dateStr,
                            category = category,
                            inputText = inputText,
                            totalCalories = result.calories,
                            totalProtein = result.protein,
                            totalCarbs = result.carbs,
                            totalFat = result.fat
                        )
                    )

                    val foods = result.foods.map {
                        Food(
                            mealId = mealId.toInt(),
                            name = it.name,
                            calories = it.calories,
                            protein = it.protein,
                            carbs = it.carbs,
                            fat = it.fat
                        )
                    }
                    repository.insertFoods(foods)

                    // Refresh food list for this date
                    val currentMap = _mealFoods.value.toMutableMap()
                    currentMap[mealId.toInt()] = foods
                    _mealFoods.value = currentMap

                    generateNutritionInsight()
                    onDone(true)
                } else {
                    // Fail gracefully by inserting a draft meal so the user can see *something* if Gemini fails
                    val mockMealId = repository.insertMeal(
                        Meal(
                            dateString = dateStr,
                            category = category,
                            inputText = inputText,
                            totalCalories = 280,
                            totalProtein = 15.0,
                            totalCarbs = 25.0,
                            totalFat = 8.0
                        )
                    )
                    val mockFoods = listOf(
                        Food(mealId = mockMealId.toInt(), name = "Comida (estimación genérica)", calories = 280, protein = 15.0, carbs = 25.0, fat = 8.0)
                    )
                    repository.insertFoods(mockFoods)
                    generateNutritionInsight()
                    onDone(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed logging natural language meal description", e)
                onDone(false)
            } finally {
                _mealAnalysisLoading.value = false
            }
        }
    }

    fun deleteMeal(mealId: Int) {
        viewModelScope.launch {
            repository.deleteMealById(mealId)
            generateNutritionInsight()
        }
    }

    // Daily Insight Generation
    fun generateNutritionInsight() {
        viewModelScope.launch {
            _dailyInsightLoading.value = true
            try {
                val config = settingsState.value
                val meals = selectedDateMeals.value
                val totalCalories = meals.sumOf { it.totalCalories }
                val totalProtein = meals.sumOf { it.totalProtein }

                val prompt = """
                    Actúa como un Nutricionista deportivo de SynergyFit.
                    El usuario tiene el siguiente objetivo fit: "${config.fitnessGoal}".
                    Hoy ha consumido: $totalCalories / ${config.targetCalories} kcal y $totalProtein / ${config.targetProtein}g de proteínas.
                    Su peso corporal es ${config.bodyWeight} kg.
                    Proporciónale 1 consejo nutricional sumamente útil, corto y directo (máximo 2 oraciones sencillas) basándote en los macros de su comida de hoy y su objetivo. 
                    Sé motivador y sofisticado, en español.
                """.trimIndent()

                val insight = GeminiService.generateResponse(prompt = prompt, settings = config)
                _dailyInsight.value = insight
            } catch (e: Exception) {
                _dailyInsight.value = "Mantente bien hidratado para mejorar la recuperación muscular."
            } finally {
                _dailyInsightLoading.value = false
            }
        }
    }

    // Coach Chat (FIFO last 5)
    fun askCoach(question: String) {
        if (question.trim().isEmpty()) return
        val config = settingsState.value

        viewModelScope.launch {
            _chatLoading.value = true
            val currentList = _chatHistory.value.toMutableList()
            currentList.add(Pair(question, "Pensando..."))
            _chatHistory.value = currentList

            try {
                // Build a short chat history prompt for the coach
                val historyContext = _chatHistory.value.dropLast(1).joinToString("\n") {
                    "Usuario: ${it.first}\nCoach: ${it.second}"
                }
                val prompt = """
                    Eres el Coach Deportivo de SynergyFit. Respondes con brevedad y sofisticación en español amoled minimalista.
                    Objetivo secundario del usuario: ${config.fitnessGoal}
                    Peso corporal: ${config.bodyWeight} kg
                    
                    Conversación histórica:
                    $historyContext
                    
                    Usuario pregunta de nuevo: "$question"
                    Responde brevemente (máximo 3 o 4 líneas) con consejos específicos de alto nivel académico para rutinas, hipertrofia o suplementación.
                """.trimIndent()

                val response = GeminiService.generateResponse(prompt = prompt, settings = config)

                val updatedList = _chatHistory.value.toMutableList()
                if (updatedList.isNotEmpty()) {
                    updatedList[updatedList.size - 1] = Pair(question, response)
                }
                // FIFO to ensure only 5 conversations maximum are tracked
                if (updatedList.size > 5) {
                    _chatHistory.value = updatedList.takeLast(5)
                } else {
                    _chatHistory.value = updatedList
                }
            } catch (e: Exception) {
                val updatedList = _chatHistory.value.toMutableList()
                if (updatedList.isNotEmpty()) {
                    updatedList[updatedList.size - 1] = Pair(question, "El coach no pudo conectarse. Revisa tu API key en los Ajustes.")
                }
                _chatHistory.value = updatedList
            } finally {
                _chatLoading.value = false
            }
        }
    }

    // Active Workout Operations
    fun startActiveWorkout(routine: Routine) {
        viewModelScope.launch {
            // Cancel any running job
            cronometerJob?.cancel()
            _elapsedSeconds.value = 0

            val dateMillis = System.currentTimeMillis()
            val newSession = Session(
                routineId = routine.id,
                routineName = routine.name,
                dateMillis = dateMillis,
                durationMinutes = 0
            )

            // We do a temporary session ID (doesn't save to Room yet until we click Save, or we can save an active session in Room)
            // Let's create it in Room directly as a draft!
            val sessionId = repository.insertSession(newSession).toInt()
            val currentSession = newSession.copy(id = sessionId)
            _activeSession.value = currentSession

            // Save activeSessionId to settings for auto-saving / persistence
            val s = repository.getSettingsSync()
            repository.saveSettings(s.copy(activeSessionId = sessionId))

            // Pre-populate with exercises in that routine
            val exercises = repository.getExercisesForRoutineSync(routine.id)
            val logs = ArrayList<SessionLog>()
            exercises.forEach { ex ->
                // Add 3 default set logs for easy typing
                for (s in 1..ex.targetSets) {
                    logs.add(
                        SessionLog(
                            sessionId = sessionId,
                            exerciseName = ex.exerciseName,
                            weightKg = 0.0,
                            reps = 0,
                            isDropset = false,
                            setIndex = s
                        )
                    )
                }
            }
            // If the routine is empty, add one mock log so there's an editable field
            if (logs.isEmpty()) {
                logs.add(SessionLog(sessionId = sessionId, exerciseName = "Ejercicio", weightKg = 0.0, reps = 0, isDropset = false, setIndex = 1))
            }

            repository.insertSessionLogs(logs)
            refreshActiveLogs()

            // Start clock
            startCronometer()
        }
    }

    fun startCustomActiveWorkout() {
        viewModelScope.launch {
            cronometerJob?.cancel()
            _elapsedSeconds.value = 0

            val newSession = Session(
                routineId = null,
                routineName = "Rutinas Rápidas",
                dateMillis = System.currentTimeMillis(),
                durationMinutes = 0
            )

            val sessionId = repository.insertSession(newSession).toInt()
            val currentSession = newSession.copy(id = sessionId)
            _activeSession.value = currentSession

            // Save activeSessionId to settings for auto-saving / persistence
            val s = repository.getSettingsSync()
            repository.saveSettings(s.copy(activeSessionId = sessionId))

            // Pre-populate with a single general log
            val initialLog = SessionLog(sessionId = sessionId, exerciseName = "Ejercicio", weightKg = 0.0, reps = 0, isDropset = false, setIndex = 1)
            repository.insertSessionLog(initialLog)
            refreshActiveLogs()

            startCronometer()
        }
    }

    private fun startCronometer() {
        cronometerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun addActiveSet(exerciseName: String) {
        val current = _activeSession.value ?: return
        viewModelScope.launch {
            try {
                val logs = _activeLogs.value.filter { it.exerciseName == exerciseName }
                val nextIndex = logs.size + 1
                val newLog = SessionLog(
                    sessionId = current.id,
                    exerciseName = exerciseName,
                    weightKg = if (logs.isNotEmpty()) logs.last().weightKg else 0.0,
                    reps = if (logs.isNotEmpty()) logs.last().reps else 0,
                    isDropset = false,
                    setIndex = nextIndex
                )
                repository.insertSessionLog(newLog)
                refreshActiveLogs()
            } catch (e: Exception) {
                Log.e(TAG, "Error in addActiveSet to DB directly", e)
            }
        }
    }

    fun addActiveDropset(exerciseName: String, parentIndex: Int) {
        val current = _activeSession.value ?: return
        viewModelScope.launch {
            try {
                val parentLog = _activeLogs.value.firstOrNull { it.exerciseName == exerciseName && it.setIndex == parentIndex }
                val newLog = SessionLog(
                    sessionId = current.id,
                    exerciseName = exerciseName,
                    weightKg = if (parentLog != null) parentLog.weightKg * 0.75 else 0.0, // standard dropset is ~25% weight drop
                    reps = if (parentLog != null) parentLog.reps else 0,
                    isDropset = true,
                    setIndex = parentIndex // Groups underneath the parent set index!
                )
                repository.insertSessionLog(newLog)
                refreshActiveLogs()
            } catch (e: Exception) {
                Log.e(TAG, "Error in addActiveDropset to DB directly", e)
            }
        }
    }

    fun updateActiveSetWeight(id: Int, weightKg: Double) {
        // Fast UI update - immediate and synchronous on the Main thread to keep the keyboard ultra responsive
        val currentLogs = _activeLogs.value.toMutableList()
        val index = currentLogs.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedLog = currentLogs[index].copy(weightKg = weightKg)
            currentLogs[index] = updatedLog
            _activeLogs.value = currentLogs

            // Safeguarded DB write using Mutex and Dispatchers.IO to prevent locking contention and race conditions
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    dbWriteMutex.withLock {
                        repository.insertSessionLog(updatedLog)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in updateActiveSetWeight to DB directly", e)
                }
            }
        }
    }

    fun updateActiveSetReps(id: Int, reps: Int) {
        // Fast UI update - immediate and synchronous on the Main thread to keep the keyboard ultra responsive
        val currentLogs = _activeLogs.value.toMutableList()
        val index = currentLogs.indexOfFirst { it.id == id }
        if (index != -1) {
            val updatedLog = currentLogs[index].copy(reps = reps)
            currentLogs[index] = updatedLog
            _activeLogs.value = currentLogs

            // Safeguarded DB write using Mutex and Dispatchers.IO to prevent locking contention and race conditions
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    dbWriteMutex.withLock {
                        repository.insertSessionLog(updatedLog)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in updateActiveSetReps to DB directly", e)
                }
            }
        }
    }

    fun deleteActiveSetLog(id: Int) {
        viewModelScope.launch {
            try {
                val logToDelete = _activeLogs.value.firstOrNull { it.id == id }
                if (logToDelete != null) {
                    repository.deleteSessionLog(logToDelete)
                    refreshActiveLogs()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in deleteActiveSetLog to DB directly", e)
            }
        }
    }

    fun addExerciseToActiveWorkout(exerciseName: String, category: String = "Otros") {
        val current = _activeSession.value ?: return
        viewModelScope.launch {
            try {
                val capitalized = exerciseName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                val newLog = SessionLog(
                    sessionId = current.id,
                    exerciseName = capitalized,
                    weightKg = 0.0,
                    reps = 0,
                    isDropset = false,
                    setIndex = 1
                )
                repository.insertSessionLog(newLog)
                refreshActiveLogs()

                // Save to global exercises registry
                repository.insertExercise(Exercise(name = capitalized, category = category))
            } catch (e: Exception) {
                Log.e(TAG, "Error in addExerciseToActiveWorkout to DB directly", e)
            }
        }
    }

    private suspend fun refreshActiveLogs() {
        val current = _activeSession.value ?: return
        _activeLogs.value = repository.getLogsForSessionSync(current.id)
    }

    fun finishActiveWorkout(backdatedOffsetDays: Int, onDone: () -> Unit) {
        val current = _activeSession.value ?: return
        cronometerJob?.cancel()

        viewModelScope.launch {
            try {
                // Apply dates
                var finalDate = System.currentTimeMillis()
                if (backdatedOffsetDays > 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -backdatedOffsetDays)
                    finalDate = cal.timeInMillis
                }

                // Filter out totally empty sets to avoid logging garbage
                val validLogs = _activeLogs.value.filter { it.weightKg > 0 && it.reps > 0 }
                
                // Delete old session draft if empty completely
                if (validLogs.isEmpty()) {
                    repository.deleteSession(current)
                } else {
                    // Keep only valid logs
                    val durationMin = (_elapsedSeconds.value / 60).coerceAtLeast(1)
                    
                    // Clear existing logs in DB and re-insert valid ones
                    repository.deleteSession(current) // this cleans everything
                    val newSessionId = repository.insertSession(
                        current.copy(
                            id = 0,
                            dateMillis = finalDate,
                            durationMinutes = durationMin
                        )
                    ).toInt()

                    val completedLogs = validLogs.map { 
                        it.copy(id = 0, sessionId = newSessionId)
                    }
                    repository.insertSessionLogs(completedLogs)

                    // Additionally ensure all logged exercises exist in public Exercise registry
                    validLogs.forEach { log ->
                        val capitalized = log.exerciseName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        repository.insertExercise(Exercise(name = capitalized, category = "Otros"))
                    }
                }

                // Complete in ViewModel
                _activeSession.value = null
                _activeLogs.value = emptyList()
                _elapsedSeconds.value = 0

                // Clear activeSessionId from settings for auto-saving / persistence
                val s = repository.getSettingsSync()
                repository.saveSettings(s.copy(activeSessionId = null))
                generateNutritionInsight()
            } catch (e: Exception) {
                Log.e(TAG, "Error finishing active workout session", e)
            } finally {
                onDone()
            }
        }
    }

    fun discardActiveWorkout() {
        val current = _activeSession.value ?: return
        cronometerJob?.cancel()
        viewModelScope.launch {
            try {
                repository.deleteSession(current)
                _activeSession.value = null
                _activeLogs.value = emptyList()
                _elapsedSeconds.value = 0

                // Clear activeSessionId from settings for auto-saving / persistence
                val s = repository.getSettingsSync()
                repository.saveSettings(s.copy(activeSessionId = null))
            } catch (e: Exception) {
                Log.e(TAG, "Error discarding active workout session", e)
            }
        }
    }

    fun loadLocalBackups() {
        _localBackupsList.value = getLocalBackups()
    }

    // Helper utilities
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun checkAndPerformMonthlyBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = Calendar.getInstance()
            if (today.get(Calendar.DAY_OF_MONTH) == 1) {
                val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(today.time)
                val filename = "backup_month_$monthFormat.json"
                val file = java.io.File(app.filesDir, filename)
                if (!file.exists()) {
                    try {
                        val json = exportJson()
                        file.writeText(json)
                        Log.d(TAG, "Copia de seguridad mensual generada con éxito en local: ${file.name}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error generando copia de seguridad mensual automática", e)
                    }
                }
            }
        }
    }

    fun getLocalBackups(): List<String> {
        val dir = app.filesDir
        val files = dir.listFiles { _, name -> name.startsWith("backup_month_") && name.endsWith(".json") }
        return files?.map { it.name }?.sortedDescending() ?: emptyList()
    }

    fun generateManualBackup(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(today.time)
            val filename = "backup_month_$dateFormat.json"
            val file = java.io.File(app.filesDir, filename)
            try {
                val json = exportJson()
                file.writeText(json)
                withContext(Dispatchers.Main) {
                    loadLocalBackups()
                    onResult("Copia guardada localmente: $filename")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Error al crear copia: ${e.localizedMessage}")
                }
            }
        }
    }

    fun restoreLocalBackup(filename: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = java.io.File(getApplication<Application>().filesDir, filename)
            if (!file.exists()) {
                withContext(Dispatchers.Main) {
                    onResult("Error: El archivo no existe")
                }
                return@launch
            }
            try {
                val json = file.readText()
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val map = moshi.adapter(Map::class.java).fromJson(json) as? Map<*, *>
                if (map != null) {
                    val userName = map["userName"] as? String ?: "Usuario"
                    val fitnessGoal = map["fitnessGoal"] as? String ?: "Hipertrofia"
                    val bodyWeightObj = map["bodyWeight"]
                    val bodyWeight = when (bodyWeightObj) {
                        is Double -> bodyWeightObj
                        is Float -> bodyWeightObj.toDouble()
                        is Number -> bodyWeightObj.toDouble()
                        else -> 70.0
                    }
                    val current = settingsState.value
                    val updated = current.copy(
                        username = userName,
                        fitnessGoal = fitnessGoal,
                        bodyWeight = bodyWeight
                    )
                    repository.saveSettings(updated)
                    withContext(Dispatchers.Main) {
                        onResult("Copia de seguridad local restaurada exitosamente.")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult("Error: Formato de copia de seguridad inválido.")
                    }
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main) {
                    onResult("Error al restaurar: ${e.localizedMessage}")
                }
            }
        }
    }

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        try {
            val settings = settingsState.value
            val meals = repository.allMeals.firstOrNull() ?: emptyList()
            val sessionsList = repository.sessions.firstOrNull() ?: emptyList()

            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val map = mapOf(
                "userName" to settings.username,
                "fitnessGoal" to settings.fitnessGoal,
                "bodyWeight" to settings.bodyWeight,
                "meals_count" to meals.size,
                "sessions_count" to sessionsList.size,
                "exportedAt" to SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            )

            return@withContext moshi.adapter(Map::class.java).toJson(map)
        } catch (e: Exception) {
            return@withContext "{\"error\": \"${e.localizedMessage}\"}"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _chatHistory.value = emptyList()
            _activeSession.value = null
            _activeLogs.value = emptyList()
            _elapsedSeconds.value = 0
            _isUserLoggedIn.value = false
        }
    }
}
