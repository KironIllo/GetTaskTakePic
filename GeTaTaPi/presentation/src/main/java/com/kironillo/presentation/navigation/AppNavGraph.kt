package com.kironillo.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.presentation.ui.auth.AuthScreen
import com.kironillo.presentation.ui.history.HistoryScreen
import com.kironillo.presentation.ui.home.HomeScreen
import com.kironillo.presentation.ui.permissions.PermissionsScreen
import com.kironillo.presentation.ui.profile.ProfileScreen
import com.kironillo.presentation.ui.settings.SettingsScreen
import com.kironillo.presentation.ui.task.ReviewScreen
import com.kironillo.presentation.ui.task.TaskScreen
import com.kironillo.presentation.viewmodel.AuthUiState
import com.kironillo.presentation.viewmodel.AuthViewModel

object AppRoutes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val HISTORY = "history"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val PERMISSIONS = "permissions"
}

@Composable
fun AppNavGraph(authViewModel: AuthViewModel = hiltViewModel()) {
    val authState by authViewModel.uiState.collectAsState()

    when (authState) {
        AuthUiState.CheckingSession -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is AuthUiState.Success -> MainNavHost(startDestination = AppRoutes.HOME)
        else -> MainNavHost(startDestination = AppRoutes.AUTH)
    }
}

@Composable
private fun MainNavHost(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(AppRoutes.AUTH) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onTaskSelected = { task ->
                    navController.navigate(
                        TaskRoute(
                            taskId = task.id,
                            topic = task.topic,
                            difficulty = task.difficulty.name,
                            targetObject = task.targetObject,
                        ),
                    )
                },
                onLogout = {
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onHistory = {
                    navController.navigate(AppRoutes.HISTORY)
                },
                onProfile = {
                    navController.navigate(AppRoutes.PROFILE)
                },
                onSettings = {
                    navController.navigate(AppRoutes.SETTINGS)
                },
            )
        }

        composable<TaskRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TaskRoute>()
            val difficulty =
                runCatching {
                    TaskDifficulty.valueOf(route.difficulty)
                }.getOrDefault(TaskDifficulty.EASY)

            TaskScreen(
                task =
                    Task(
                        id = route.taskId,
                        topic = route.topic,
                        difficulty = difficulty,
                        targetObject = route.targetObject,
                    ),
                onBack = { navController.popBackStack() },
                onPhotoSelected = { uri ->
                    navController.navigate(
                        ReviewRoute(
                            taskId = route.taskId,
                            photoUri = uri.toString(),
                        ),
                    )
                },
            )
        }

        composable<ReviewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReviewRoute>()

            ReviewScreen(
                taskId = route.taskId,
                photoUri = Uri.parse(route.photoUri),
                onBack = { navController.popBackStack() },
                onGoHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }

        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onSettings = { navController.navigate(AppRoutes.SETTINGS) },
                onLoggedOut = {
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                // Передаем колбэк для перехода на экран разрешений
                onNavigateToPermissions = { navController.navigate(AppRoutes.PERMISSIONS) },
            )
        }

        // <-- ДОБАВЛЕН НОВЫЙ ЭКРАН РАЗРЕШЕНИЙ
        composable(AppRoutes.PERMISSIONS) {
            PermissionsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
