package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FitScreen
import com.example.ui.FitViewModel
import com.example.ui.theme.FitNeonGreen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: FitViewModel) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Pulse animation for tagline & logo
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Rotate dumbbell
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    // Trigger navigation after delay
    LaunchedEffect(Unit) {
        delay(2600)
        viewModel.navigateTo(FitScreen.LoginSignUp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF0A0F00), // A very deep neon lime hint glow
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High fidelity custom Canvas athletic dumbbell logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Outer neon circular indicator
                    drawArc(
                        color = FitNeonGreen.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                    
                    rotate(angle) {
                        // Dumbbell metal shaft
                        drawLine(
                            color = Color.White,
                            start = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.5f),
                            end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.5f),
                            strokeWidth = 14.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        
                        // Dumbbell weights - Left Plate
                        drawRoundRect(
                            color = FitNeonGreen,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.3f),
                            size = androidx.compose.ui.geometry.Size(12.dp.toPx(), h * 0.4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.35f),
                            size = androidx.compose.ui.geometry.Size(6.dp.toPx(), h * 0.3f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
 
                        // Dumbbell weights - Right Plate
                        drawRoundRect(
                            color = FitNeonGreen,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.73f, h * 0.3f),
                            size = androidx.compose.ui.geometry.Size(12.dp.toPx(), h * 0.4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color.White,
                            topLeft = androidx.compose.ui.geometry.Offset(w * 0.67f, h * 0.35f),
                            size = androidx.compose.ui.geometry.Size(6.dp.toPx(), h * 0.3f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title with distinct neon italic display font
            Text(
                text = "FITMAX",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 6.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "EST. 2026",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Tagline
            Text(
                text = "“Transform Your Body,\nTransform Your Life”",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 26.sp,
                    letterSpacing = 0.5.sp,
                    color = Color.White.copy(alpha = scale)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )
        }
    }
}
