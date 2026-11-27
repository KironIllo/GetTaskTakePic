package com.kironillo.presentation.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.presentation.ui.components.ErrorState
import com.kironillo.presentation.ui.components.InlineError
import com.kironillo.presentation.viewmodel.HomeUiState
import com.kironillo.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onTaskSelected: (Task) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    onHistory: () -> Unit,
    onProfile: () -> Unit,
    onSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val selectedTopic by viewModel.selectedTopicId.collectAsState()
    val topicsError by viewModel.topicsError.collectAsState()
    val difficulty by viewModel.selectedDifficulty.collectAsState()
    val creating by viewModel.isCreatingTask.collectAsState()
    val createError by viewModel.createTaskError.collectAsState()
    val logoutError by viewModel.logoutError.collectAsState()
    val loggingOut by viewModel.isLoggingOut.collectAsState()
    val createTaskRetry: () -> Unit = { viewModel.createTask(onTaskSelected) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshTasks() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomItem("Главная", true, {})
                BottomItem("Статистика", false, onHistory)
                BottomItem("Карта", false, {}, enabled = false)
                BottomItem("Настройки", false, onSettings)
            }
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
        ) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("GeTaTaPi", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onProfile) { Text("Профиль") }
                    TextButton(onClick = { viewModel.logout(onLogout) }, enabled = !loggingOut) {
                        if (loggingOut) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Выйти", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(16.dp),
                        ).padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🔥", fontSize = 22.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("Количество дней", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "Выбор Темы",
                    Modifier.padding(top = 34.dp, bottom = 18.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (topics.isEmpty() && topicsError != null) {
                    ErrorState(
                        message = topicsError.orEmpty(),
                        onRetry = { viewModel.loadTopics() },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                } else if (topics.isEmpty()) {
                    Text("Загружаем темы…", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
                } else {
                    topics.chunked(2).forEach { rowTopics ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            rowTopics.forEach { topic ->
                                val selected = topic.id == selectedTopic
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(76.dp)
                                        .padding(bottom = 8.dp)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(12.dp),
                                        ).clickable { viewModel.selectTopic(topic.id) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        topic.title,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                }
                            }
                            if (rowTopics.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Text(
                    "Сложность",
                    Modifier.padding(start = 6.dp, top = 16.dp, bottom = 16.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DifficultyChip("Низкая", TaskDifficulty.EASY, difficulty, Modifier.weight(1f), viewModel::selectDifficulty)
                    DifficultyChip("Средняя", TaskDifficulty.MEDIUM, difficulty, Modifier.weight(1f), viewModel::selectDifficulty)
                    DifficultyChip("Высокая", TaskDifficulty.HARD, difficulty, Modifier.weight(1f), viewModel::selectDifficulty)
                }
                Button(
                    onClick = { viewModel.createTask(onTaskSelected) },
                    enabled = !creating && selectedTopic != null,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 22.dp).height(58.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    if (creating) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                            Text("Придумываем задание…", fontSize = 16.sp)
                        }
                    } else {
                        Text("Получить задание", fontSize = 16.sp)
                    }
                }
                createError?.let { message ->
                    InlineError(
                        message = message,
                        onRetry = if (selectedTopic != null && !creating) createTaskRetry else null,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                logoutError?.let { message ->
                    InlineError(message = message, modifier = Modifier.padding(bottom = 8.dp))
                }

                // Previously available tasks remain accessible below the mockup's primary content.
                when (val state = uiState) {
                    HomeUiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    is HomeUiState.Content -> {
                        Text(
                            "Мои задания",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                        )
                        state.tasks.forEach { task ->
                            Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onTaskSelected(task) }) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(task.topic, fontWeight = FontWeight.SemiBold)
                                    Text(task.targetObject)
                                    Text("Сложность: ${difficultyLabel(task.difficulty)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    HomeUiState.Empty -> Unit
                    is HomeUiState.Error ->
                        ErrorState(
                            message = state.message,
                            onRetry = { viewModel.loadTasks() },
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    label: String,
    value: TaskDifficulty,
    selected: TaskDifficulty,
    modifier: Modifier,
    onSelect: (TaskDifficulty) -> Unit,
) {
    Box(
        modifier
            .height(52.dp)
            .background(
                if (value ==
                    selected
                ) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                RoundedCornerShape(16.dp),
            ).clickable { onSelect(value) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color =
                if (value ==
                    selected
                ) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            fontSize = 13.sp,
            fontWeight =
                if (value ==
                    selected
                ) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
        )
    }
}

@Composable
private fun BottomItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Text(
        label,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick).padding(horizontal = 4.dp, vertical = 4.dp),
        color =
            if (!enabled) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
            } else if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        fontSize = 14.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
    )
}

private fun difficultyLabel(value: TaskDifficulty): String =
    when (value) {
        TaskDifficulty.EASY -> "Низкая"
        TaskDifficulty.MEDIUM -> "Средняя"
        TaskDifficulty.HARD -> "Высокая"
    }
