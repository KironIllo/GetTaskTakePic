package com.kironillo.presentation.ui.task

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kironillo.presentation.ui.components.PrimaryButton
import com.kironillo.presentation.viewmodel.ReviewUiState
import com.kironillo.presentation.viewmodel.ReviewViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReviewScreen(
    taskId: String,
    photoUri: Uri,
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val photoResult by produceState<Result<Bitmap>?>(
        initialValue = null,
        key1 = photoUri,
    ) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    val decoded =
                        if (photoUri.scheme == "file") {
                            BitmapFactory.decodeFile(photoUri.path)
                        } else {
                            context.contentResolver.openInputStream(photoUri)?.use {
                                BitmapFactory.decodeStream(it)
                            }
                        }
                    decoded ?: error("Не удалось прочитать фотографию")
                }
            }
    }
    val bitmap = photoResult?.getOrNull()

    // По макету экран проверки открывается сразу после выбора фотографии.
    // Поэтому отправляем её автоматически, когда Bitmap уже прочитан.
    LaunchedEffect(taskId, photoUri, photoResult) {
        if (bitmap != null && uiState is ReviewUiState.Idle) {
            viewModel.submitPhoto(
                taskId = taskId,
                photoPath = photoUri.toString(),
            )
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.width(48.dp),
                    ) {
                        Text("×", style = MaterialTheme.typography.headlineMedium)
                    }
                }

                when {
                    photoResult == null -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    bitmap == null -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Не удалось загрузить фотографию",
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    else -> {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Выбранная фотография",
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                when (val state = uiState) {
                    ReviewUiState.Idle,
                    ReviewUiState.Loading,
                    is ReviewUiState.Processing,
                    -> {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(20.dp),
                        )
                        Text(
                            "Задание проверяется",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (state is ReviewUiState.Loading) {
                                "Отправляем фотографию..."
                            } else {
                                "Подождите ... ..."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }

                    is ReviewUiState.Success -> {
                        Text(
                            if (state.result.passed == true) {
                                "Задание выполнено!"
                            } else {
                                "Задание не пройдено"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        state.result.feedback?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(it, textAlign = TextAlign.Center)
                        }
                        state.result.confidence?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Уверенность: ${(it * 100).toInt()}%")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        PrimaryButton(
                            text = "На главный экран",
                            onClick = onGoHome,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                        )
                    }

                    is ReviewUiState.Error -> {
                        // Диалог ошибки выводится ниже поверх экрана.
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            if (photoResult?.isFailure == true) {
                AlertDialog(
                    onDismissRequest = onBack,
                    title = { Text("Неудача") },
                    text = { Text("Не удалось загрузить выбранную фотографию") },
                    confirmButton = {
                        TextButton(onClick = onBack) { Text("Закрыть") }
                    },
                )
            }

            if (uiState is ReviewUiState.Error) {
                val error = (uiState as ReviewUiState.Error).message
                AlertDialog(
                    onDismissRequest = onBack,
                    title = { Text("Неудача") },
                    text = {
                        Text(
                            error.ifBlank {
                                "Во время проверки что-то пошло не так, сорян"
                            },
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.submitPhoto(
                                    taskId = taskId,
                                    photoPath = photoUri.toString(),
                                )
                            },
                        ) {
                            Text("Повторить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onBack) {
                            Text("Закрыть")
                        }
                    },
                )
            }
        }
    }
}
