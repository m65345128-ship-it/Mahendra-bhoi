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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WeightLog
import com.example.ui.FitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(viewModel: FitViewModel) {
    val weightLogs by viewModel.weightLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val exercises by viewModel.exercises.collectAsState()
    val totalWater by viewModel.totalWaterMl.collectAsState()
    val waterGoal by viewModel.recommendedWaterMl.collectAsState()

    var inputWeight by remember { mutableStateOf("75.0") }

    // Draft measurements
    var chestMeas by remember { mutableStateOf(104f) }
    var backMeas by remember { mutableStateOf(112f) }
    var waistMeas by remember { mutableStateOf(82f) }
    var armsMeas by remember { mutableStateOf(38f) }
    var thighsMeas by remember { mutableStateOf(58f) }

    // Simulated progress avatar file uploader simulation
    val milestoneIcons = listOf("🦁", "🐯", "🐆", "🦍", "🦅", "🐺")
    var selectedMilestoneIndex by remember { mutableStateOf(2) }

    // Computations / Badges unlocked triggers
    val firstWorkoutUnlocked = exercises.any { it.isCompletedToday }
    val hydrationHeroLocked = totalWater >= waterGoal
    val userBmi = if (profile.heightCm > 0) (profile.weightKg / ((profile.heightCm / 100f) * (profile.heightCm / 100f))) else 22f
    val normalBmiUnlocked = userBmi >= 18.5f && userBmi < 25f
    val streakKingUnlocked = (if (firstWorkoutUnlocked) 1 else 0) + (if (hydrationHeroLocked) 1 else 0) + (if (profile.stepCountSimulated >= 10000) 1 else 0) >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("progress_screen")
    ) {
        // WEIGHT LEVEL HISTORY CHART GRAPH
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WEIGHT METRIC PROGRESSION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Current: ${profile.weightKg} kg (Targeting ${if (profile.goal == "Build Muscle") "Bulk" else "Lean"})",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Plus log
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful custom Weight Line Chart on Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    if (weightLogs.size < 2) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Add at least 2 weight entry logs to plot curves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        WeightHistoryCanvas(weightLogs = weightLogs)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fast logger row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Log Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val w = inputWeight.toFloatOrNull()
                            if (w != null && w > 0) {
                                viewModel.addWeightLog(w)
                            } else {
                                viewModel.showToast("Please enter a valid weight parameter!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    ) {
                        Text("Log", color = MaterialTheme.colorScheme.background, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // PHYSICAL BODY MEASUREMENTS RECORD
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "BODY MEASUREMENTS LOG",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Track your size index proportions regularly",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chest Slider
                LabeledMeasurementSlider(label = "Chest Circumference", unit = "cm", value = chestMeas, range = 70f..150f) { chestMeas = it }
                LabeledMeasurementSlider(label = "Back Width", unit = "cm", value = backMeas, range = 70f..160f) { backMeas = it }
                LabeledMeasurementSlider(label = "Waist Line", unit = "cm", value = waistMeas, range = 50f..130f) { waistMeas = it }
                LabeledMeasurementSlider(label = "Active Arms Range", unit = "cm", value = armsMeas, range = 20f..60f) { armsMeas = it }
                LabeledMeasurementSlider(label = "Thighs circumference", unit = "cm", value = thighsMeas, range = 30f..90f) { thighsMeas = it }

                Button(
                    onClick = { viewModel.showToast("Body measurements indexes updated in statistics logs!") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Proportions", fontWeight = FontWeight.Bold)
                }
            }
        }

        // PROGRESS PHOTO AVATAR SELECTOR
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "PROGRESS MILESTONE AVATARS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                )
                Text(
                    text = "Simulating progress profile snaps. Choose your spirit mascot:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    milestoneIcons.forEachIndexed { idx, symbol ->
                        val selected = idx == selectedMilestoneIndex
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.15f),
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    selectedMilestoneIndex = idx
                                    viewModel.showToast("Mascot set as your active physique avatar target!")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(symbol, fontSize = 24.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Selected Symbol", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(milestoneIcons[selectedMilestoneIndex])
                    }
                }
            }
        }

        // ACHIEVEMENT BADGES SHOWCASE
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ACHIEVED FITNESS RECOGNITIONS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Automated badges observe your active logging consistency:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Badge 1 - First Workout Complete
                    BadgeItemRow(
                        name = "Fit Commencement",
                        desc = "Logged completed workout exercise activity today",
                        isUnlocked = firstWorkoutUnlocked,
                        symbol = "🔥"
                    )

                    // Badge 2 - Hydration Master
                    BadgeItemRow(
                        name = "Hydration Overlord",
                        desc = "Logged water intake matching daily requirement goals",
                        isUnlocked = hydrationHeroLocked,
                        symbol = "🐳"
                    )

                    // Badge 3 - Healthy BMI index
                    BadgeItemRow(
                        name = "Eco equilibrium",
                        desc = "Your physical proportions reside in 'Normal BMI' values",
                        isUnlocked = normalBmiUnlocked,
                        symbol = "🎯"
                    )

                    // Badge 4 - Streak Master
                    BadgeItemRow(
                        name = "Streak King",
                        desc = "Complete at least 2 distinct metrics (e.g. water, workout, steps)",
                        isUnlocked = streakKingUnlocked,
                        symbol = "👑"
                    )
                }
            }
        }

        // REPORTS WRAPPER
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📋", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WEEKLY PERFORMANCE ANALYSIS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "You registered excellent metabolic synthesis this block. Active reps generated custom muscle density signals. Water hydration indexes are solid.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WeightHistoryCanvas(weightLogs: List<WeightLog>) {
    val brandPrimary = MaterialTheme.colorScheme.primary
    val logs = weightLogs.sortedBy { it.timestamp }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val maxWeight = logs.maxOf { it.weightKg }
        val minWeight = logs.minOf { it.weightKg }
        val weightDiff = (maxWeight - minWeight).coerceAtLeast(1f)

        val minTime = logs.minOf { it.timestamp }
        val maxTime = logs.maxOf { it.timestamp }
        val timeDiff = (maxTime - minTime).coerceAtLeast(1L)

        // Map and draw lines
        val points = logs.mapIndexed { idx, log ->
            val x = if (logs.size > 1) {
                ((log.timestamp - minTime).toFloat() / timeDiff) * (w - 40.dp.toPx()) + 20.dp.toPx()
            } else {
                w / 2f
            }
            // Invert y axis so higher weights are closer to top
            val y = h - 30.dp.toPx() - (((log.weightKg - minWeight) / weightDiff) * (h - 60.dp.toPx()))
            androidx.compose.ui.geometry.Offset(x, y)
        }

        // Draw grid lines
        for (i in 0..3) {
            val gridY = h - 30.dp.toPx() - (i * (h - 60.dp.toPx()) / 3f)
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.15f),
                start = androidx.compose.ui.geometry.Offset(20.dp.toPx(), gridY),
                end = androidx.compose.ui.geometry.Offset(w - 20.dp.toPx(), gridY),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw path with fill gradient below curve
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, h)
                for (pt in points) {
                    lineTo(pt.x, pt.y)
                }
                lineTo(points.last().x, h)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        brandPrimary.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                )
            )

            // Draw line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (pt in points) {
                    lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = linePath,
                color = brandPrimary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Draw point dots and text indicators
        points.forEachIndexed { idx, pt ->
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = brandPrimary,
                radius = 4.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun LabeledMeasurementSlider(
    label: String,
    unit: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = String.format("%.1f %s", value, unit), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = Color.DarkGray.copy(alpha = 0.3f),
                thumbColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun BadgeItemRow(
    name: String,
    desc: String,
    isUnlocked: Boolean,
    symbol: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isUnlocked) MaterialTheme.colorScheme.background else Color.DarkGray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Badge container
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                fontSize = 24.sp,
                modifier = Modifier.clip(CircleShape),
                color = if (isUnlocked) Color.Unspecified else Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else Color.Gray
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .background(
                    if (isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.DarkGray.copy(alpha = 0.2f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (isUnlocked) "UNLOCKED" else "LOCKED",
                color = if (isUnlocked) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }
    }
}
