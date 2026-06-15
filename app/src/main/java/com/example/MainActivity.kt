package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.FitRepository
import com.example.ui.FitScreen
import com.example.ui.FitViewModel
import com.example.ui.screens.*
import com.example.ui.theme.FitMaxTheme

class MainActivity : ComponentActivity() {
    private lateinit var database: AppDatabase
    private lateinit var repository: FitRepository
    private lateinit var viewModel: FitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize Room Local Database
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fitmax_database"
        ).fallbackToDestructiveMigration().build()

        repository = FitRepository(database.fitDao())
        
        // 2. Instantiate master VM
        viewModel = FitViewModel(repository)

        enableEdgeToEdge()

        setContent {
            val profile by viewModel.userProfile.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val uiMessage by viewModel.uiMessage.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            // Observe VM toasts and dispatch via standard M3 Snackbar popup
            LaunchedEffect(uiMessage) {
                uiMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearToast()
                }
            }

            // 3. Central Color Scheme wrapper reacting to user's profile settings
            FitMaxTheme(darkTheme = profile.isDarkMode) {
                val backgroundFill = MaterialTheme.colorScheme.background

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundFill
                ) {
                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        topBar = {
                            if (currentScreen != FitScreen.Splash && currentScreen != FitScreen.LoginSignUp) {
                                FitAppBar(
                                    currentScreen = currentScreen,
                                    isPremium = profile.isPremium,
                                    onBackClick = { viewModel.navigateTo(FitScreen.Home) }
                                )
                            }
                        },
                        bottomBar = {
                            if (currentScreen != FitScreen.Splash && currentScreen != FitScreen.LoginSignUp) {
                                FitBottomBar(
                                    currentScreen = currentScreen,
                                    onTabSelected = { viewModel.navigateTo(it) }
                                )
                            }
                        },
                        contentWindowInsets = WindowInsets.safeDrawing,
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(backgroundFill)
                        ) {
                            // 4. Custom animated screen router transition
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                label = "screen_router"
                            ) { target ->
                                when (target) {
                                    FitScreen.Splash -> SplashScreen(viewModel)
                                    FitScreen.LoginSignUp -> LoginScreen(viewModel)
                                    FitScreen.Home -> DashboardScreen(viewModel)
                                    FitScreen.Workout -> WorkoutScreen(viewModel)
                                    FitScreen.Diet -> DietScreen(viewModel)
                                    FitScreen.Progress -> ProgressScreen(viewModel)
                                    FitScreen.Tools -> ToolsScreen(viewModel)
                                    FitScreen.Notifications -> NotificationScreen(viewModel)
                                    FitScreen.Profile -> ProfileScreen(viewModel)
                                    FitScreen.AICoach -> AICoachScreen(viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitAppBar(
    currentScreen: FitScreen,
    isPremium: Boolean,
    onBackClick: () -> Unit
) {
    val titleText = when (currentScreen) {
        FitScreen.Home -> "GYMGURU DASHBOARD"
        FitScreen.Workout -> "GYMGURU TRAINING"
        FitScreen.Diet -> "GYMGURU DIET PLANNER"
        FitScreen.Progress -> "METRICS TRACKING"
        FitScreen.Tools -> "FITNESS TOOLS"
        FitScreen.Notifications -> "ALERTS & HISTORY"
        FitScreen.Profile -> "PROFILE SETTINGS"
        FitScreen.AICoach -> "GYMGURU COACH"
        else -> "GYMGURU"
    }

    val isSubScreen = currentScreen == FitScreen.Notifications || currentScreen == FitScreen.Profile || currentScreen == FitScreen.AICoach

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )

                if (isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFB300), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "PRO",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (isSubScreen) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Return home")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground
        ),
        modifier = Modifier.testTag("fit_app_bar")
    )
}

@Composable
fun FitBottomBar(
    currentScreen: FitScreen,
    onTabSelected: (FitScreen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("fit_bottom_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen == FitScreen.Home,
            onClick = { onTabSelected(FitScreen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == FitScreen.Workout,
            onClick = { onTabSelected(FitScreen.Workout) },
            icon = { Icon(Icons.Default.Star, contentDescription = "Workouts") },
            label = { Text("Workout", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == FitScreen.Diet,
            onClick = { onTabSelected(FitScreen.Diet) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Diet") },
            label = { Text("Diet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == FitScreen.Progress,
            onClick = { onTabSelected(FitScreen.Progress) },
            icon = { Icon(Icons.Default.List, contentDescription = "Progress") },
            label = { Text("Progress", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
        NavigationBarItem(
            selected = currentScreen == FitScreen.Tools,
            onClick = { onTabSelected(FitScreen.Tools) },
            icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
            label = { Text("Tools", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
        )
    }
}
