package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.data.MealItem
import com.example.ui.FitScreen
import com.example.ui.FitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(viewModel: FitViewModel) {
    val meals by viewModel.meals.collectAsState()
    val bmrGoal by viewModel.bmrValue.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    // Filter properties
    var selectedMealTypeTab by remember { mutableStateOf("All") } // "All", "Breakfast", "Lunch", "Dinner", "Snack"
    var filterVegetarianOnly by remember { mutableStateOf(false) }

    // Dialog form
    var showCreateDialog by remember { mutableStateOf(false) }
    var mealName by remember { mutableStateOf("") }
    var mealTypeForm by remember { mutableStateOf("Breakfast") }
    var mealCal by remember { mutableStateOf("350") }
    var mealProt by remember { mutableStateOf("20") }
    var mealCarb by remember { mutableStateOf("45") }
    var mealFat by remember { mutableStateOf("8") }
    var mealIsVeg by remember { mutableStateOf(true) }

    val eatenMeals = meals.filter { it.isEatenToday }
    val loggedCalories = eatenMeals.sumOf { it.calories }
    val loggedProtein = eatenMeals.sumOf { it.proteinG }
    val loggedCarbs = eatenMeals.sumOf { it.carbsG }
    val loggedFat = eatenMeals.sumOf { it.fatG }

    val targetCalories = when(profile.goal) {
        "Build Muscle" -> bmrGoal + 400
        "Lose Weight" -> bmrGoal - 400
        "Lean Bulk" -> bmrGoal + 200
        else -> bmrGoal
    }

    val tabs = listOf("All", "Breakfast", "Lunch", "Dinner", "Snack")

    // Filter matching meals list
    val filteredMeals = meals.filter { meal ->
        val matchesTab = selectedMealTypeTab == "All" || meal.mealType == selectedMealTypeTab
        val matchesVeg = !filterVegetarianOnly || meal.isVegetarian
        matchesTab && matchesVeg
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("diet_screen")
    ) {
        // NUTRITION METRICS SUMMARY HEADER
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "DAILY PROGRESS & CALORIC TARGET",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$loggedCalories",
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black)
                            )
                            Text(
                                text = " / $targetCalories kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                        Text(
                            text = "Daily nutritional requirement status",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Simple Circular indicator ratio
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = if (targetCalories > 0) loggedCalories.toFloat() / targetCalories else 0f,
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 5.dp
                        )
                        Text(
                            text = "${if (targetCalories > 0) (loggedCalories * 100 / targetCalories) else 0}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Linear progression meters for custom proteins
                val targetProtein = (profile.weightKg * 2f).coerceAtLeast(100f).toInt()
                val targetCarbs = when(profile.goal) {
                    "Lose Weight" -> 150
                    else -> 280
                }
                val targetFat = 70

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacroProgressLine(name = "Protein", value = loggedProtein, target = targetProtein, color = MaterialTheme.colorScheme.primary)
                    MacroProgressLine(name = "Carbohydrates", value = loggedCarbs, target = targetCarbs, color = Color(0xFFFFB300))
                    MacroProgressLine(name = "Fat lipids", value = loggedFat, target = targetFat, color = Color(0xFFFF4757))
                }
            }
        }

        // Ask AI Meal Coach Banner
        Card(
            onClick = { viewModel.navigateTo(FitScreen.AICoach) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(FitScreen.AICoach) }
                .padding(bottom = 16.dp)
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
                        "AI DIET RECIPE PLANNER",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "Ask Guru to prescribe a lunch plan or low-calorie high-protein dinner!",
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

        // SECTOR FILTER PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Veg toggle Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable { filterVegetarianOnly = !filterVegetarianOnly }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(if (filterVegetarianOnly) Color(0xFF2E7D32) else Color.DarkGray, RoundedCornerShape(2.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vegetarian only", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Create custom dish action
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Meal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // TABS FOR BREAKFAST, LUNCH, DINNER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedMealTypeTab
                Card(
                    onClick = { selectedMealTypeTab = tab },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.border(
                        1.dp,
                        if (selected) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                ) {
                    Text(
                        text = tab,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // MEALS LOGGED SCROLLABLE AREA
        if (filteredMeals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥦", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No meal profiles detected.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "No meals match this filter active. Try toggling vegetarian filter or log a custom dish using the top button!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredMeals) { meal ->
                    MealListRow(
                        meal = meal,
                        onEatenToggle = { viewModel.toggleMealEaten(meal.id, it) },
                        onDelete = { viewModel.deleteMeal(meal.id) }
                    )
                }
            }
        }
    }

    // CREATE CUSTOM DIET DISH DIALOG
    if (showCreateDialog) {
        val formMealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Log Custom Dish", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = { Text("Meal Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Meal Type selector
                    Column {
                        Text("Meal Slot", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            formMealTypes.forEach { type ->
                                FilterChip(
                                    selected = mealTypeForm == type,
                                    onClick = { mealTypeForm = type },
                                    label = { Text(type) }
                                )
                            }
                        }
                    }

                    // Macro fields
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mealCal,
                            onValueChange = { mealCal = it },
                            label = { Text("Kcal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mealProt,
                            onValueChange = { mealProt = it },
                            label = { Text("Prot (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mealCarb,
                            onValueChange = { mealCarb = it },
                            label = { Text("Carb (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mealFat,
                            onValueChange = { mealFat = it },
                            label = { Text("Fat (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { mealIsVeg = !mealIsVeg }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = mealIsVeg, onCheckedChange = { mealIsVeg = it })
                        Text("This is a vegetarian option", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val calories = mealCal.toIntOrNull() ?: 350
                        val protein = mealProt.toIntOrNull() ?: 15
                        val carbs = mealCarb.toIntOrNull() ?: 45
                        val fats = mealFat.toIntOrNull() ?: 8
                        viewModel.createCustomMeal(
                            name = mealName,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fats,
                            isVeg = mealIsVeg,
                            type = mealTypeForm
                        )
                        showCreateDialog = false
                        mealName = "" // Reset
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save & Add To Log", color = MaterialTheme.colorScheme.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MacroProgressLine(
    name: String,
    value: Int,
    target: Int,
    color: Color
) {
    val ratio = if (target > 0) (value.toFloat() / target).coerceIn(0f, 1f) else 0f
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$value / $target g", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun MealListRow(
    meal: MealItem,
    onEatenToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = meal.isEatenToday,
                onCheckedChange = onEatenToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = meal.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (meal.isEatenToday) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (meal.isVegetarian) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (meal.isVegetarian) "V" else "N",
                            color = if (meal.isVegetarian) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${meal.mealType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    Text("•", fontSize = 11.sp, color = Color.Gray)
                    Text("${meal.calories} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", fontSize = 11.sp, color = Color.Gray)
                    Text("P: ${meal.proteinG}g C: ${meal.carbsG}g F: ${meal.fatG}g", fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Delete option for custom plates
            if (meal.customCreated) {
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove Meal", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
