package com.kironillo.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kironillo.presentation.ui.home.HomeScreen

object AppRoutes {
    const val HOME = "home"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
    ) {
        composable(AppRoutes.HOME) {
            HomeScreen()
        }
    }
}
