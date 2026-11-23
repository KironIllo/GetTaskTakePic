package com.kironillo.presentation.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kironillo.domain.model.Task
import com.kironillo.presentation.viewmodel.HomeUiState
import com.kironillo.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onTaskSelected: (Task) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    onHistory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val logoutError by viewModel.logoutError.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refreshTasks()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp),
        ) {
            Text(
                text = "GeTaTaPi",
                style = MaterialTheme.typography.headlineMedium,
            )

            Button(
                onClick = {
                    viewModel.logout(onLogout)
                },
                enabled = !isLoggingOut,
                modifier = Modifier.padding(top = 12.dp),
            ) {
                if (isLoggingOut) {
                    CircularProgressIndicator()
                } else {
                    Text("Выйти из аккаунта")
                }
            }

            OutlinedButton(
                onClick = onHistory,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("История проверок")
            }

            logoutError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text(
                text = "Твои задания",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            when (val state = uiState) {
                HomeUiState.Loading -> {
                    LoadingContent()
                }

                is HomeUiState.Content -> {
                    TasksContent(state.tasks, onTaskSelected)
                }

                HomeUiState.Empty -> {
                    Text("Пока нет заданий. Создай своё первое задание!")
                }

                is HomeUiState.Error -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(state.message)
                        Button(onClick = viewModel::loadTasks) {
                            Text("Повторить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = "Загружаем задания...",
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun TasksContent(
    tasks: List<Task>,
    onTaskSelected: (Task) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tasks, key = { it.id }) { task ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onTaskSelected(task) },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = task.topic,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(task.targetObject)
                    Text(
                        text = "Сложность: ${task.difficulty.name}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
