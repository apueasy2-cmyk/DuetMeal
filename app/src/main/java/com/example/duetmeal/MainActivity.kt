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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import com.example.duetmeal.util.NotificationHelper
class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        NotificationHelper.createNotificationChannel(this)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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

    // Notice Notification logic
    val notices by viewModel.notices.collectAsState()
    val context = LocalContext.current
    var lastNoticeIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(notices) {
        val newNoticeIds = notices.map { it.id }.toSet()
        if (lastNoticeIds.isNotEmpty()) {
            val newUnread = notices.filter { it.isUnread && it.id !in lastNoticeIds }
            if (newUnread.isNotEmpty()) {
                val title = if (newUnread.size == 1) newUnread.first().title else "New Notices"
                val content = if (newUnread.size == 1) newUnread.first().content else "You have ${newUnread.size} new notices"
                com.example.duetmeal.util.NotificationHelper.showNoticeNotification(context, title, content)
            }
        }
        lastNoticeIds = newNoticeIds
    }

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

                // ── Notice Detail Screen ──────────────────────────────────────
                composable(Screen.NoticeDetail.route) { backStackEntry ->
                    val noticeId = backStackEntry.arguments?.getString("noticeId") ?: ""
                    NoticeDetailScreen(
                        noticeId = noticeId,
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
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
