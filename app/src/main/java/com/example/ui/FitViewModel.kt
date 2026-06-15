package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FitScreen {
    Splash,
    LoginSignUp,
    Home,
    Workout,
    Diet,
    Progress,
    Tools,
    Notifications,
    Profile,
    AICoach
}

data class ChatMessage(val sender: String, val content: String, val timestamp: Long = System.currentTimeMillis())

class FitViewModel(private val repository: FitRepository) : ViewModel() {

    // AICoach State Management
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI Coach",
                content = "👋 Hello! I am your GymGuru AI Assistant. I can help recommend workout exercises, create diet plans, or answer any fitness and hydration questions you have!\n\nI have automatically loaded your active physical profile to tailor advice just for you. How can I help you today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Exercise Coach States for human explanation
    private val _exerciseExplanation = MutableStateFlow<String?>(null)
    val exerciseExplanation: StateFlow<String?> = _exerciseExplanation.asStateFlow()

    private val _isExerciseExplanationLoading = MutableStateFlow(false)
    val isExerciseExplanationLoading: StateFlow<Boolean> = _isExerciseExplanationLoading.asStateFlow()

    fun clearExerciseExplanation() {
        _exerciseExplanation.value = null
    }

    fun requestExerciseExplanationHuman(exerciseName: String, customQuestion: String? = null) {
        _isExerciseExplanationLoading.value = true
        _exerciseExplanation.value = null
        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val questionPrompt = if (customQuestion.isNullOrBlank()) {
                    "Explain proper form, postural alignment, correct breathing, muscle tracking, and common mistakes."
                } else {
                    "The user has a custom question: \"$customQuestion\""
                }

                val prompt = """
                    I need a detailed, high-performance explanation for the exercise: "$exerciseName".
                    Tone: Real human personal trainer, empathetic, encouraging, and elite.
                    
                    Explain:
                    1. Proper Skeletal Alignment & Movement (with focus points)
                    2. Respiration & Breathing Cycle (when to inhale, when to exhale relative to the workload squeeze)
                    3. Safety warnings / Common Mistakes to prevent joint injury.
                    
                    $questionPrompt
                """.trimIndent()

                val sysInstruction = """
                    You are GymGuru Human AI Coach. You specialize in giving crystal-clear, safe, biomechanically correct forms of movement for $exerciseName.
                    Address the user (${profile.name}) with elite trainer precision. Keep it highly readable with bullet points.
                """.trimIndent()

                val response = com.example.data.GeminiService.generateContent(prompt, sysInstruction)
                _exerciseExplanation.value = response
            } catch (e: Exception) {
                _exerciseExplanation.value = "Unable to reach GymGuru training feed: ${e.localizedMessage}. Please try again!"
            } finally {
                _isExerciseExplanationLoading.value = false
            }
        }
    }

    fun clearChat() {
        val name = userProfile.value.name
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "AI Coach",
                content = "Hello $name! Your chat session is reset. Ask me for a customized workout, calorie recommendation, or protein snack recipe!"
            )
        )
    }

    fun sendChatMessage(prompt: String) {
        if (prompt.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(sender = "User", content = prompt))
        _chatMessages.value = currentList

        _isAiLoading.value = true
        viewModelScope.launch {
            try {
                val profile = userProfile.value
                val sysInstruction = """
                    You are GymGuru AI Coach, an elite personal trainer and expert sports nutritionist. 
                    You are advising a user named ${profile.name} (Gender: ${profile.gender}, Age: ${profile.age} years old, Height: ${profile.heightCm} cm, Weight: ${profile.weightKg} kg, Fitness Goal: ${profile.goal}, Activity Level: ${profile.activityLevel}).
                    Provide specific, encouraging, highly scientific, actionable training advice, workout exercises with complete sets/reps/rest, or daily recipes with detailed macros based on their particulars.
                    Keep paragraphs concise, use clear headings, formatting, and standard bullet points.
                """.trimIndent()
                
                val aiResponse = com.example.data.GeminiService.generateContent(prompt, sysInstruction)
                val updatedList = _chatMessages.value.toMutableList()
                updatedList.add(ChatMessage(sender = "AI Coach", content = aiResponse))
                _chatMessages.value = updatedList
            } catch (e: Exception) {
                val updatedList = _chatMessages.value.toMutableList()
                updatedList.add(ChatMessage(sender = "AI Coach", content = "I had trouble connecting to the trainer channel: ${e.localizedMessage}. Please try again!"))
                _chatMessages.value = updatedList
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // Target UI Screen
    private val _currentScreen = MutableStateFlow(FitScreen.Splash)
    val currentScreen: StateFlow<FitScreen> = _currentScreen.asStateFlow()

    // Splash navigation flow helper
    fun navigateTo(screen: FitScreen) {
        _currentScreen.value = screen
    }

    // UI Toast/Alert messages
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    fun showToast(message: String) {
        _uiMessage.value = message
        viewModelScope.launch {
            delay(3000)
            if (_uiMessage.value == message) {
                _uiMessage.value = null
            }
        }
    }

    fun clearToast() {
        _uiMessage.value = null
    }

    // Auth simulation
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun login(email: String, provider: String = "Email") {
        viewModelScope.launch {
            _isAuthenticated.value = true
            showToast("Successfully signed in via $provider as $email")
            delay(600)
            _currentScreen.value = FitScreen.Home
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentScreen.value = FitScreen.LoginSignUp
        showToast("Logged out successfully.")
    }

    // Date formatting helper
    val currentDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Database Flows
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val weightLogs: StateFlow<List<WeightLog>> = repository.weightLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meals: StateFlow<List<MealItem>> = repository.meals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<ExerciseItem>> = repository.exercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotifLog>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Water tracker (reactive to date changes)
    private val _selectedDate = MutableStateFlow(currentDateStr)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val waterLogs: StateFlow<List<WaterLog>> = _selectedDate
        .flatMapLatest { date -> repository.getWaterLogsFlow(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWaterMl: StateFlow<Int> = waterLogs
        .map { logs -> logs.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // active Workout Tracker States
    private val _activeWorkoutCategory = MutableStateFlow("Chest")
    val activeWorkoutCategory: StateFlow<String> = _activeWorkoutCategory.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("Beginner") // Beginner, Intermediate, Advanced
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    fun setWorkoutFilter(category: String, level: String) {
        _activeWorkoutCategory.value = category
        _selectedDifficulty.value = level
    }

    // Active rest countdown timer state
    private val _restSecondsLeft = MutableStateFlow(0)
    val restSecondsLeft: StateFlow<Int> = _restSecondsLeft.asStateFlow()

    private val _restTimerMax = MutableStateFlow(60)
    val restTimerMax: StateFlow<Int> = _restTimerMax.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private var timerJob: Job? = null

    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _restTimerMax.value = seconds
        _restSecondsLeft.value = seconds
        _timerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_restSecondsLeft.value > 0 && _timerRunning.value) {
                delay(1000)
                _restSecondsLeft.value -= 1
            }
            _timerRunning.value = false
            if (_restSecondsLeft.value == 0) {
                showToast("Rest timer complete! Start your next set!")
                repository.addNotification("Rest Over!", "Get ready for your next workout set!", "Workout")
            }
        }
    }

    fun pauseRestTimer() {
        _timerRunning.value = false
        timerJob?.cancel()
    }

    fun resumeRestTimer() {
        if (_restSecondsLeft.value > 0) {
            _timerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_restSecondsLeft.value > 0 && _timerRunning.value) {
                    delay(1000)
                    _restSecondsLeft.value -= 1
                }
                _timerRunning.value = false
            }
        }
    }

    fun cancelRestTimer() {
        _timerRunning.value = false
        _restSecondsLeft.value = 0
        timerJob?.cancel()
    }

    // Tools State & Computations
    // 1. BMI
    private val _bmiHeight = MutableStateFlow(175f) // cm
    val bmiHeight: StateFlow<Float> = _bmiHeight.asStateFlow()

    private val _bmiWeight = MutableStateFlow(75f) // kg
    val bmiWeight: StateFlow<Float> = _bmiWeight.asStateFlow()

    fun updateBmiParams(height: Float, weight: Float) {
        _bmiHeight.value = height
        _bmiWeight.value = weight
    }

    val bmiValue: StateFlow<Float> = combine(_bmiHeight, _bmiWeight) { h, w ->
        if (h > 0) (w / ((h / 100f) * (h / 100f))) else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24.5f)

    // 2. BMR (Harris-Benedict)
    val bmrValue: StateFlow<Int> = userProfile.map { profile ->
        // Men: BMR = 88.362 + (13.397 x weight in kg) + (4.799 x height in cm) - (5.677 x age in years)
        // Women: BMR = 447.593 + (9.247 x weight in kg) + (3.098 x height in cm) - (4.330 x age in years)
        val w = profile.weightKg
        val h = profile.heightCm
        val a = profile.age.toFloat()
        val isMale = profile.gender.equals("male", ignoreCase = true)
        val bmr = if (isMale) {
            88.362f + (13.397f * w) + (4.799f * h) - (5.677f * a)
        } else {
            447.593f + (9.247f * w) + (3.098f * h) - (4.330f * a)
        }
        bmr.toInt()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1750)

    // 3. Water intake calculator recommended (based on weight in lbs/kg)
    val recommendedWaterMl: StateFlow<Int> = userProfile.map { profile ->
        // ~35 ml per kg of bodyweight, with active multiplier
        val base = profile.weightKg * 35f
        val bonus = if (profile.activityLevel == "Very Active") 1000f else if (profile.activityLevel == "Active") 500f else 0f
        (base + bonus).toInt().coerceAtLeast(1500)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    // Step Simulator
    fun addSimulatedSteps(steps: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val newSteps = prof.stepCountSimulated + steps
            val updated = prof.copy(stepCountSimulated = newSteps)
            repository.saveProfile(updated)
            if (newSteps >= 10000 && prof.stepCountSimulated < 10000) {
                repository.addNotification("Goal Achieved!", "Woohoo! You reached your 10k daily step goal!", "Motivation")
                showToast("🏆 Badge Unlocked: Step Master! 10k Daily steps achieved.")
            }
        }
    }

    // Database Initialization and Seeding
    init {
        viewModelScope.launch {
            // Check if profile exists, seed database if not
            val existingProfile = repository.getProfileDirect()
            if (existingProfile == null) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        // 1. Profile
        val defaultProfile = UserProfile(
            name = "Alex Rivera",
            age = 26,
            gender = "Male",
            heightCm = 178f,
            weightKg = 76.5f,
            goal = "Build Muscle",
            activityLevel = "Active",
            isPremium = false,
            isDarkMode = true,
            stepCountSimulated = 5400
        )
        repository.saveProfile(defaultProfile)

        // 2. Weight logs (for historical chart)
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        repository.addWeightLog(78.5f, now - 28 * dayMillis)
        repository.addWeightLog(78.1f, now - 21 * dayMillis)
        repository.addWeightLog(77.6f, now - 14 * dayMillis)
        repository.addWeightLog(77.0f, now - 7 * dayMillis)
        repository.addWeightLog(76.5f, now)

        // 3. Exercises
        listOf(
            ExerciseItem(name = "Barbell Bench Press", category = "Chest", reps = 10, sets = 4, restSec = 90, difficulty = "Intermediate", animationType = "press"),
            ExerciseItem(name = "Incline Dumbbell Flyes", category = "Chest", reps = 12, sets = 3, restSec = 60, difficulty = "Beginner", animationType = "press"),
            ExerciseItem(name = "Chest Cable Dips", category = "Chest", reps = 8, sets = 4, restSec = 90, difficulty = "Advanced", animationType = "pull"),

            ExerciseItem(name = "Wide Lat Pulldowns", category = "Back", reps = 12, sets = 4, restSec = 75, difficulty = "Beginner", animationType = "pull"),
            ExerciseItem(name = "Bent Over Barbell Rows", category = "Back", reps = 8, sets = 4, restSec = 90, difficulty = "Advanced", animationType = "pull"),
            ExerciseItem(name = "Hyperextensions", category = "Back", reps = 15, sets = 3, restSec = 60, difficulty = "Beginner", animationType = "core"),

            ExerciseItem(name = "Overhead Dumbbell Press", category = "Shoulders", reps = 10, sets = 3, restSec = 75, difficulty = "Intermediate", animationType = "press"),
            ExerciseItem(name = "Lateral Dumbbell Raises", category = "Shoulders", reps = 15, sets = 3, restSec = 60, difficulty = "Beginner", animationType = "lift"),
            ExerciseItem(name = "Reverse Cable Pec Fly", category = "Shoulders", reps = 12, sets = 3, restSec = 60, difficulty = "Intermediate", animationType = "pull"),

            ExerciseItem(name = "Alternate Bicep Curl", category = "Arms", reps = 12, sets = 3, restSec = 60, difficulty = "Beginner", animationType = "pull"),
            ExerciseItem(name = "Overhead Tricep Extension", category = "Arms", reps = 10, sets = 3, restSec = 60, difficulty = "Intermediate", animationType = "lift"),
            ExerciseItem(name = "Dumbbell Hammer Curl", category = "Arms", reps = 12, sets = 3, restSec = 60, difficulty = "Beginner", animationType = "pull"),

            ExerciseItem(name = "Barbell Back Squat", category = "Legs", reps = 8, sets = 4, restSec = 120, difficulty = "Intermediate", animationType = "squat"),
            ExerciseItem(name = "Romanian Deadlift", category = "Legs", reps = 10, sets = 4, restSec = 90, difficulty = "Advanced", animationType = "squat"),
            ExerciseItem(name = "Seated Calf Raises", category = "Legs", reps = 15, sets = 3, restSec = 45, difficulty = "Beginner", animationType = "lift"),

            ExerciseItem(name = "Hanging Knee Raises", category = "Core", reps = 15, sets = 3, restSec = 45, difficulty = "Intermediate", animationType = "core"),
            ExerciseItem(name = "Plank Hold", category = "Core", reps = 60, sets = 3, restSec = 45, difficulty = "Beginner", animationType = "core"),

            ExerciseItem(name = "Kettlebell Swing", category = "Full Body", reps = 15, sets = 4, restSec = 60, difficulty = "Intermediate", animationType = "cardio"),
            ExerciseItem(name = "Dumbbell Thrusters", category = "Full Body", reps = 10, sets = 3, restSec = 90, difficulty = "Advanced", animationType = "squat")
        ).forEach { repository.addExercise(it) }

        // 4. Meals
        listOf(
            MealItem(name = "Protein Oatmeal & Banana", mealType = "Breakfast", calories = 450, proteinG = 32, carbsG = 55, fatG = 8, isVegetarian = true),
            MealItem(name = "Scrambled Eggs & Avocado Toast", mealType = "Breakfast", calories = 410, proteinG = 22, carbsG = 26, fatG = 22, isVegetarian = false),
            
            MealItem(name = "Grilled Chicken & Brown Rice", mealType = "Lunch", calories = 580, proteinG = 45, carbsG = 65, fatG = 12, isVegetarian = false),
            MealItem(name = "Quinoa Salad with Spiced Tofu", mealType = "Lunch", calories = 420, proteinG = 20, carbsG = 48, fatG = 16, isVegetarian = true),
            
            MealItem(name = "Baked Salmon with Asparagus", mealType = "Dinner", calories = 520, proteinG = 38, carbsG = 35, fatG = 22, isVegetarian = false),
            MealItem(name = "Spicy Red Lentil Penne", mealType = "Dinner", calories = 485, proteinG = 26, carbsG = 68, fatG = 6, isVegetarian = true),
            
            MealItem(name = "Gourmet Almonds & Clean Whey", mealType = "Snack", calories = 260, proteinG = 28, carbsG = 8, fatG = 11, isVegetarian = true)
        ).forEach { repository.addMeal(it) }

        // 5. Water
        repository.addWaterLog(500, currentDateStr)
        repository.addWaterLog(250, currentDateStr)

        // 6. Notifications
        listOf(
            NotifLog(title = "Morning Nutrition Reminder!", message = "Ensure you log your breakfast oatmeal to stay on top of proteins today!", category = "Diet", isRead = false),
            NotifLog(title = "Chest Day Awaits!", message = "Focus on clean reps and a controlled tempo. Unleash your potential!", category = "Workout", isRead = false),
            NotifLog(title = "Stay Hydrated!", message = "Your body requires mineral synthesis to heal muscle tissue. Take a sip of water now.", category = "Motivation", isRead = true)
        ).forEach { repository.insertNotification(it) }
    }

    // Water tracker operations
    fun logWaterPlus(amountMl: Int) {
        viewModelScope.launch {
            repository.addWaterLog(amountMl, currentDateStr)
            showToast("Added $amountMl ml water")
            // Badge trigger
            val newTotal = (totalWaterMl.value) + amountMl
            if (newTotal >= recommendedWaterMl.value) {
                repository.addNotification("Hydration Achieved!", "Way to go! You hit your water requirements for the day", "Diet")
                showToast("🏆 Badge Unlocked: Hydration Hero! Daily intake milestone met.")
            }
        }
    }

    fun logWaterMinus() {
        viewModelScope.launch {
            repository.clearWaterLogs(currentDateStr)
            showToast("Cleared water intake for today")
        }
    }

    // Weight logs operations
    fun addWeightLog(weight: Float) {
        if (weight <= 0) return
        viewModelScope.launch {
            repository.addWeightLog(weight, System.currentTimeMillis())
            // Update profile weight as well
            val updated = userProfile.value.copy(weightKg = weight)
            repository.saveProfile(updated)
            showToast("Weight logged: $weight kg")
        }
    }

    fun removeWeightLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWeightLog(id)
            showToast("Log entry removed")
        }
    }

    // Diet operations
    fun toggleMealEaten(mealId: Long, isEaten: Boolean) {
        viewModelScope.launch {
            repository.setMealEaten(mealId, isEaten)
            if (isEaten) {
                showToast("Meal logged as eaten!")
            }
        }
    }

    fun createCustomMeal(name: String, calories: Int, protein: Int, carbs: Int, fat: Int, isVeg: Boolean, type: String) {
        if (name.isBlank() || calories <= 0) {
            showToast("Invalid meal parameters!")
            return
        }
        viewModelScope.launch {
            val custom = MealItem(
                name = name,
                mealType = type,
                calories = calories,
                proteinG = protein,
                carbsG = carbs,
                fatG = fat,
                isVegetarian = isVeg,
                customCreated = true,
                isEatenToday = true
            )
            repository.addMeal(custom)
            showToast("Custom $type added and logged!")
        }
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            repository.deleteMealId(id)
            showToast("Meal item removed")
        }
    }

    // Workout operations
    fun toggleWorkoutExercise(exerciseId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.setExerciseCompleted(exerciseId, isCompleted)
            if (isCompleted) {
                showToast("Exercise completed! Keep pushing!")
                // Count completed
                val completedCount = (exercises.value.filter { it.isCompletedToday }.size) + 1
                if (completedCount == 1) {
                    repository.addNotification("Workout Started!", "First exercise completed today. Keep going!", "Workout")
                    showToast("🏆 Badge Unlocked: First workout exercise logged today.")
                }
            }
        }
    }

    fun createCustomExercise(name: String, category: String, reps: Int, sets: Int, restSec: Int, level: String) {
        if (name.isBlank() || reps <= 0 || sets <= 0) {
            showToast("Invalid exercise parameters!")
            return
        }
        viewModelScope.launch {
            val custom = ExerciseItem(
                name = name,
                category = category,
                reps = reps,
                sets = sets,
                restSec = restSec,
                difficulty = level,
                isCompletedToday = false,
                animationType = "press"
            )
            repository.addExercise(custom)
            showToast("Custom exercise added to $category!")
        }
    }

    fun resetAllWorkouts() {
        viewModelScope.launch {
            repository.resetAllWorkouts()
            showToast("Today's exercise progression reset.")
        }
    }

    // Profile Updates
    fun updateProfile(name: String, age: Int, gender: String, height: Float, weight: Float, goal: String, activity: String) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                name = name,
                age = age,
                gender = gender,
                heightCm = height,
                weightKg = weight,
                goal = goal,
                activityLevel = activity
            )
            repository.saveProfile(updated)
            showToast("Fitness settings saved!")
        }
    }

    // Premium Subscription Switch
    fun purchasePremium() {
        viewModelScope.launch {
            val updated = userProfile.value.copy(isPremium = true)
            repository.saveProfile(updated)
            showToast("👑 Premium PRO Activated! Enjoy unrestricted features!")
            repository.addNotification("Subscription Active!", "Thank you for supporting FitMax Pro. Your premium features are fully unlocked!", "Motivation")
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = userProfile.value
            val toggled = current.copy(isDarkMode = !current.isDarkMode)
            repository.saveProfile(toggled)
            showToast(if (toggled.isDarkMode) "Dark Mode Activated" else "Light Mode Activated")
        }
    }

    // Notifications Operations
    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showToast("All reminders marked as read")
        }
    }

    fun addManualMotivation() {
        viewModelScope.launch {
            val quotes = listOf(
                "Pain is temporary. Pride is forever.",
                "Your mind is your most important gym equipment.",
                "Do something today that your future self will thank you for.",
                "Consistency wins the marathon of fitness, not intensity.",
                "Don't wish for a good body. Work for it!"
            )
            val selected = quotes.random()
            repository.addNotification("Daily Inspiration", selected, "Motivation")
            showToast("New motivation quote received!")
        }
    }
}
