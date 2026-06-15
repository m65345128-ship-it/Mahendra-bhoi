package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.ExerciseItem
import com.example.ui.FitScreen
import com.example.ui.FitViewModel
import com.example.ui.theme.FitNeonGreen
import com.example.ui.theme.FitElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: FitViewModel) {
    val exercises by viewModel.exercises.collectAsState()
    val activeCategory by viewModel.activeWorkoutCategory.collectAsState()
    val activeDifficulty by viewModel.selectedDifficulty.collectAsState()
    
    // Rest Timer states
    val secondsLeft by viewModel.restSecondsLeft.collectAsState()
    val maxTimer by viewModel.restTimerMax.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()

    // Exercise Coach states for human explanation
    var activeExplanationExercise by remember { mutableStateOf<ExerciseItem?>(null) }
    val exerciseExplanation by viewModel.exerciseExplanation.collectAsState()
    val isExerciseExplanationLoading by viewModel.isExerciseExplanationLoading.collectAsState()

    LaunchedEffect(activeExplanationExercise) {
        activeExplanationExercise?.let { exercise ->
            viewModel.requestExerciseExplanationHuman(exercise.name)
        }
    }

    // Form states
    var showCreateDialog by remember { mutableStateOf(false) }
    var exerciseName by remember { mutableStateOf("") }
    var exerciseCat by remember { mutableStateOf(activeCategory) }
    var exerciseReps by remember { mutableStateOf("10") }
    var exerciseSets by remember { mutableStateOf("4") }
    var exerciseRest by remember { mutableStateOf("90") }
    var exerciseDiff by remember { mutableStateOf(activeDifficulty) }

    val filteredList = exercises.filter {
        it.category.lowercase() == activeCategory.lowercase() &&
        it.difficulty.lowercase() == activeDifficulty.lowercase()
    }

    val categories = listOf("Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Full Body")
    val difficulties = listOf("Beginner", "Intermediate", "Advanced")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("workout_screen")
    ) {
        // Horizontal Muscle Categories Filter
        Text(
            text = "MUSCLE GROUP PROFILE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val selected = category == activeCategory
                Card(
                    onClick = { viewModel.setWorkoutFilter(category, activeDifficulty) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.border(
                        1.dp,
                        if (selected) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Difficulty Program Selector
        Text(
            text = "TRAINING PROGRAM MODE",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            difficulties.forEach { level ->
                val selected = level == activeDifficulty
                Card(
                    onClick = { viewModel.setWorkoutFilter(activeCategory, level) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = level,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ask AI Workout Coach Banner
        Card(
            onClick = { viewModel.navigateTo(FitScreen.AICoach) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(FitScreen.AICoach) }
                .padding(bottom = 12.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "GYMGURU AI WORKOUT BUILDER",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Click to ask your trainer to customize high-performance circuits for $activeCategory!",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Float Actions Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$activeCategory Routine - $activeDifficulty ($filteredList size)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Reset Workout progression
                IconButton(
                    onClick = { viewModel.resetAllWorkouts() },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset progression", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
                
                // Add button Custom exercise
                IconButton(
                    onClick = { 
                        exerciseCat = activeCategory
                        exerciseDiff = activeDifficulty
                        showCreateDialog = true 
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add custom workout", tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ACTIVE REST COUNTDOWN TIMER BLOCK
        AnimatedVisibility(
            visible = secondsLeft > 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer circular track
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = secondsLeft.toFloat() / maxTimer,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "$secondsLeft",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE REST COUNTDOWN",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Muscle synthesis and oxygenating...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Play / Pause
                        IconButton(
                            onClick = { 
                                if (timerRunning) viewModel.pauseRestTimer() else viewModel.resumeRestTimer() 
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                .size(36.dp)
                        ) {
                            Text(
                                text = if (timerRunning) "⏸" else "▶",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Close/Skip
                        IconButton(
                            onClick = { viewModel.cancelRestTimer() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Skip timer", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Exercises ListView
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No drills mapped here yet.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Tap the '+' button to log custom exercise drills for $activeCategory!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { drill ->
                    ExerciseCard(
                        drill = drill,
                        onToggle = { viewModel.toggleWorkoutExercise(drill.id, it) },
                        onStartTimer = { viewModel.startRestTimer(drill.restSec) },
                        onExplainClicked = { activeExplanationExercise = drill }
                    )
                }
            }
        }
    }

    // Modal Sheet Dialog to Create Workout Exercise
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "Add Custom Exercise",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Exercise Drill Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Muscle category Selection
                    Column {
                        Text("Muscle Target", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { c ->
                                FilterChip(
                                    selected = exerciseCat == c,
                                    onClick = { exerciseCat = c },
                                    label = { Text(c) }
                                )
                            }
                        }
                    }

                    // Sets and Reps inputs
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = exerciseSets,
                            onValueChange = { exerciseSets = it },
                            label = { Text("Sets") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = exerciseReps,
                            onValueChange = { exerciseReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Rest Timer input
                    OutlinedTextField(
                        value = exerciseRest,
                        onValueChange = { exerciseRest = it },
                        label = { Text("Rest Period (seconds)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Difficulty Level Selection
                    Column {
                        Text("Routine Difficulty", style = MaterialTheme.typography.labelSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            difficulties.forEach { level ->
                                FilterChip(
                                    selected = exerciseDiff == level,
                                    onClick = { exerciseDiff = level },
                                    label = { Text(level) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reps = exerciseReps.toIntOrNull() ?: 10
                        val sets = exerciseSets.toIntOrNull() ?: 4
                        val rest = exerciseRest.toIntOrNull() ?: 60
                        viewModel.createCustomExercise(
                            name = exerciseName,
                            category = exerciseCat,
                            reps = reps,
                            sets = sets,
                            restSec = rest,
                            level = exerciseDiff
                        )
                        showCreateDialog = false
                        // Reset input
                        exerciseName = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Exercise", color = MaterialTheme.colorScheme.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Interactive AI Human Form Guide & Ask Analyst Dialog
    if (activeExplanationExercise != null) {
        val exercise = activeExplanationExercise!!
        var userQuestion by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { 
                viewModel.clearExerciseExplanation()
                activeExplanationExercise = null 
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "${exercise.name}",
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "GYMGURU FORM POSTURE COACH",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    // Explanation response view
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            if (isExerciseExplanationLoading) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Consulting Guru human biomechanics libraries...",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            } else {
                                val text = exerciseExplanation ?: "Press below to query Guru's human movement libraries."
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Real interaction asking box
                    Text(
                        text = "ASK THE GURU COACH AN EXTRA QUESTION:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("e.g. How to prevent back fatigue?", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("custom_coach_question"),
                        maxLines = 2,
                        textStyle = MaterialTheme.typography.bodySmall,
                        trailingIcon = {
                            if (userQuestion.isNotBlank() && !isExerciseExplanationLoading) {
                                IconButton(
                                    onClick = {
                                        viewModel.requestExerciseExplanationHuman(exercise.name, userQuestion)
                                        // Clear input
                                        userQuestion = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )

                    // Default recommendations tags to quick tap
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Avoid arching?",
                            "Core bracing?",
                            "Breathing tips?",
                            "Alternative guide?"
                        ).forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.requestExerciseExplanationHuman(
                                        exerciseName = exercise.name,
                                        customQuestion = "Regarding ${exercise.name}, tell me: $tag"
                                    )
                                },
                                label = { Text(tag, fontSize = 9.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearExerciseExplanation()
                        activeExplanationExercise = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Close Guide", color = MaterialTheme.colorScheme.background)
                }
            }
        )
    }
}

@Composable
fun ExerciseCard(
    drill: ExerciseItem,
    onToggle: (Boolean) -> Unit,
    onStartTimer: () -> Unit,
    onExplainClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .border(
                1.dp,
                if (drill.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Left Content: Icon and details
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(
                                if (drill.isCompletedToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when(drill.category.lowercase()) {
                                "chest" -> "💪"
                                "back" -> "👐"
                                "arms" -> "🖖"
                                "legs" -> "🦿"
                                "core" -> "🧘"
                                "shoulders" -> "🛡️"
                                else -> "🏋️"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = drill.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (drill.isCompletedToday) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${drill.sets} Sets x ${drill.reps} Reps • Rest ${drill.restSec}s",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Check action checkbox
                Checkbox(
                    checked = drill.isCompletedToday,
                    onCheckedChange = onToggle,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.background
                    )
                )
            }

            // Expanded animation section / interactive Rest timers
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 12.dp))
                    
                    Text(
                        text = "ANIMATION ILLUSTRATION GUIDE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real anim on canvas!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Call canvas drawing
                            AnimatedDrillCanvas(drill.animationType)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Trigger Rest Timer Action Box
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Sets Progression:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                // Little dots representing sets
                                for (i in 1..drill.sets) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                if (drill.isCompletedToday) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                                                CircleShape
                                            )
                                    )
                                }
                            }
                            Text(
                                "Interval target: ${drill.restSec} seconds",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onStartTimer,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Rest", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = onExplainClicked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp).weight(1f)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Form Coach", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedDrillCanvas(animationType: String) {
    val transition = rememberInfiniteTransition(label = "drill_anim")
    
    // Scale or offset variable
    val pulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        when(animationType.lowercase()) {
            "press" -> {
                // Draw bench chest press action: bar sliding up/down with dynamic trails
                // Bench frame line
                drawLine(
                    color = Color.DarkGray,
                    start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.75f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.75f),
                    strokeWidth = 3.dp.toPx()
                )
                // Barbell moving up/down based on pulse
                val currentY = h * 0.3f + (h * 0.35f * (1f - pulse))
                drawLine(
                    color = FitNeonGreen,
                    start = androidx.compose.ui.geometry.Offset(w * 0.15f, currentY),
                    end = androidx.compose.ui.geometry.Offset(w * 0.85f, currentY),
                    strokeWidth = 5.dp.toPx()
                )
                // Barbell weight plates
                drawCircle(Color.White, radius = 10.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.15f, currentY))
                drawCircle(Color.White, radius = 10.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.85f, currentY))
            }
            "pull" -> {
                // Lat pulldown cable action: handle moving down, cable lines stretching
                // Top pulley support
                drawCircle(Color.DarkGray, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.2f))
                val barY = h * 0.3f + (h * 0.45f * pulse)
                // Cable lines
                drawLine(
                    color = Color.Gray,
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.2f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, barY),
                    strokeWidth = 2.dp.toPx()
                )
                // Pulldown bar (bending slightly at ends)
                drawLine(
                    color = FitElectricBlue,
                    start = androidx.compose.ui.geometry.Offset(w * 0.2f, barY),
                    end = androidx.compose.ui.geometry.Offset(w * 0.8f, barY),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            "squat" -> {
                // Squat action: back/spine lines pivoting, hips descending
                val squatFactor = pulse
                val thighY = h * 0.5f + (h * 0.2f * squatFactor)
                
                // Torso
                drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.32f), end = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.52f), strokeWidth = 5.dp.toPx())
                // Thigh line (moves with squatting factor)
                drawLine(FitNeonGreen, start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.52f), end = androidx.compose.ui.geometry.Offset(w * 0.65f, thighY), strokeWidth = 4.dp.toPx())
                // Head
                drawCircle(Color.White, radius = 8.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.24f))
            }
            "cardio" -> {
                // Cardio pulse waves
                drawCircle(
                    color = Color(0xFFFF4757).copy(alpha = 1f - pulse),
                    radius = (w * 0.4f) * pulse,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFF4757),
                    radius = 16.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
                )
            }
            else -> {
                // Generic core activity: expanding power rings
                drawArc(
                    color = FitNeonGreen,
                    startAngle = 180f * pulse,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                    size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.6f),
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f)
                )
                drawCircle(Color.White, radius = 6.dp.toPx(), center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f))
            }
        }
    }
}
