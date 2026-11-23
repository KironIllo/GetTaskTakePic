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
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.presentation.ui.auth.AuthScreen
import com.kironillo.presentation.ui.history.HistoryScreen
import com.kironillo.presentation.ui.home.HomeScreen
import com.kironillo.presentation.ui.task.ReviewScreen
import com.kironillo.presentation.ui.task.TaskScreen
import com.kironillo.presentation.viewmodel.AuthUiState
import com.kironillo.presentation.viewmodel.AuthViewModel

object AppRoutes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val TASK = "task/{taskId}/{topic}/{difficulty}/{targetObject}"
    const val REVIEW = "review/{taskId}/{photoUri}"

    const val HISTORY = "history"

    fun taskRoute(task: Task): String =
        "task/${Uri.encode(task.id)}/${Uri.encode(task.topic)}/${task.difficulty.name}/${Uri.encode(task.targetObject)}"

    fun reviewRoute(
        taskId: String,
        uri: Uri,
    ): String = "review/${Uri.encode(taskId)}/${Uri.encode(uri.toString())}"
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

        is AuthUiState.Success -> {
            MainNavHost(startDestination = AppRoutes.HOME)
        }

        else -> {
            MainNavHost(startDestination = AppRoutes.AUTH)
        }
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
                    navController.navigate(AppRoutes.taskRoute(task))
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
            )
        }

        composable(AppRoutes.TASK) { backStackEntry ->
            val taskId =
                Uri.decode(
                    backStackEntry.arguments?.getString("taskId").orEmpty(),
                )
            val topic =
                Uri.decode(
                    backStackEntry.arguments?.getString("topic").orEmpty(),
                )
            val difficulty =
                TaskDifficulty.valueOf(
                    backStackEntry.arguments?.getString("difficulty")
                        ?: TaskDifficulty.EASY.name,
                )
            val targetObject =
                Uri.decode(
                    backStackEntry.arguments?.getString("targetObject").orEmpty(),
                )

            TaskScreen(
                task =
                    Task(
                        id = taskId,
                        topic = topic,
                        difficulty = difficulty,
                        targetObject = targetObject,
                    ),
                onBack = { navController.popBackStack() },
                onPhotoSelected = { uri ->
                    navController.navigate(AppRoutes.reviewRoute(taskId, uri))
                },
            )
        }

        composable(AppRoutes.REVIEW) { backStackEntry ->
            val taskId =
                Uri.decode(
                    backStackEntry.arguments?.getString("taskId").orEmpty(),
                )
            val photoUri =
                Uri.parse(
                    Uri.decode(
                        backStackEntry.arguments?.getString("photoUri").orEmpty(),
                    ),
                )

            ReviewScreen(
                taskId = taskId,
                photoUri = photoUri,
                onBack = { navController.popBackStack() },
                onRetake = { navController.popBackStack() },
                onChooseAnother = { navController.popBackStack() },
                onGoHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.HISTORY) {
            HistoryScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
