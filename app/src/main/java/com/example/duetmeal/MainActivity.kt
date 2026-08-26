package com.example.duetmeal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.duetmeal.data.DuetMealViewModel
import com.example.duetmeal.ui.components.DUETBottomNavigationBar
import com.example.duetmeal.ui.components.Screen
import com.example.duetmeal.ui.screens.*
import com.example.duetmeal.ui.theme.DUETMealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DUETMealTheme {
                DUETMealApp()
            }
        }
    }
}

@Composable
fun DUETMealApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Login.route

    // Single ViewModel instance shared across all screens
    val viewModel: DuetMealViewModel = viewModel()
    val user by viewModel.user.collectAsState()
    val initials = user?.initials ?: "FH"

    // Hide bottom navigation bar on Login screen
    val shouldShowBottomBar = currentRoute != Screen.Login.route

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                DUETBottomNavigationBar(
                    currentRoute = currentRoute,
                    initials = initials,
                    onNavigate = { route ->
                        if (route == Screen.Home.route) {
                            val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                            if (!popped) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (shouldShowBottomBar) innerPadding else androidx.compose.foundation.layout.PaddingValues())
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route
            ) {
                // ── Login Screen ─────────────────────────────────────────────
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = viewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ── Dashboard / Home Screen ───────────────────────────────────
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // ── Booking Screen ───────────────────────────────────────────
                composable(Screen.Booking.route) {
                    BookingScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // ── History Screen ───────────────────────────────────────────
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // ── Payments Screen ──────────────────────────────────────────
                composable(Screen.Payments.route) {
                    PaymentsScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // ── Notices Screen ───────────────────────────────────────────
                composable(Screen.Notices.route) {
                    NoticesScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigate = { route ->
                            navController.navigate(route)
                        }
                    )
                }

                // ── Profile Screen ───────────────────────────────────────────
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigate = { route ->
                            if (route == Screen.Login.route) {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }
        }
    }
}
