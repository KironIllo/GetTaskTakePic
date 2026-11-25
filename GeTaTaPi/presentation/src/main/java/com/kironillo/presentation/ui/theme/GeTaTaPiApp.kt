package com.kironillo.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.kironillo.presentation.navigation.AppNavGraph
import com.kironillo.presentation.viewmodel.ThemeViewModel

@Composable
fun GeTaTaPiApp(themeViewModel: ThemeViewModel = hiltViewModel()) {
    val mode by themeViewModel.themeMode.collectAsState()
    val dark =
        when (mode) {
            "DARK" -> true
            "LIGHT" -> false
            else -> isSystemInDarkTheme()
        }
    val view = LocalView.current
    SideEffect {
        val activity = view.context as? Activity
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    GeTaTaPiTheme(darkTheme = dark) {
        AppNavGraph()
    }
}
