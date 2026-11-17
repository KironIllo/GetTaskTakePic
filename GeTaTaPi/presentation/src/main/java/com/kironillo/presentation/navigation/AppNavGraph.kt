package com.kironillo.presentation.navigation

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
import com.kironillo.presentation.ui.auth.AuthScreen
import com.kironillo.presentation.ui.home.HomeScreen
import com.kironillo.presentation.viewmodel.AuthUiState
import com.kironillo.presentation.viewmodel.AuthViewModel

object AppRoutes {
    const val AUTH = "auth"
    const val HOME = "home"
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
            MainNavHost(
                startDestination = AppRoutes.HOME,
            )
        }

        else -> {
            MainNavHost(
                startDestination = AppRoutes.AUTH,
            )
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
                        popUpTo(AppRoutes.AUTH) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(AppRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(AppRoutes.AUTH) {
                        popUpTo(AppRoutes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
