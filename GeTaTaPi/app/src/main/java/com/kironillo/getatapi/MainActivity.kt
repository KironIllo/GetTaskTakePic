package com.kironillo.getatapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kironillo.presentation.navigation.AppNavGraph
import com.kironillo.presentation.ui.theme.GeTaTaPiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GeTaTaPiTheme {
                AppNavGraph()
            }
        }
    }
}
