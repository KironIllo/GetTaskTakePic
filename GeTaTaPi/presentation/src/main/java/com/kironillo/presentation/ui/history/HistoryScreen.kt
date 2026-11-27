package com.kironillo.presentation.ui.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kironillo.domain.model.SubmissionHistoryItem
import com.kironillo.presentation.history.HistoryViewModel
import com.kironillo.presentation.ui.components.ErrorState
import com.kironillo.presentation.ui.components.InlineError
import com.kironillo.presentation.viewmodel.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var selectedItem by remember {
        mutableStateOf<SubmissionHistoryItem?>(null)
    }

    var fullScreenBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История проверок") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Назад")
                    }
                },
            )
        },
    ) { padding ->

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val current = state) {
                HistoryUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is HistoryUiState.Error -> {
                    ErrorState(
                        message = current.message,
                        onRetry = viewModel::loadHistory,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is HistoryUiState.Success -> {
                    if (current.items.isEmpty()) {
                        Text(
                            "История пока пуста",
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        val listState = rememberLazyListState()

                        LaunchedEffect(
                            listState.layoutInfo.visibleItemsInfo
                                .lastOrNull()
                                ?.index,
                            current.items.size,
                        ) {
                            val lastVisible =
                                listState
                                    .layoutInfo
                                    .visibleItemsInfo
                                    .lastOrNull()
                                    ?.index ?: 0

                            if (
                                lastVisible >= current.items.size - 3 &&
                                current.hasMore
                            ) {
                                viewModel.loadNextPage()
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(
                                items = current.items,
                                key = { it.id },
                            ) { item ->

                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedItem = item
                                            },
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        HistoryPhoto(
                                            storagePath = item.storagePath,
                                            viewModel = viewModel,
                                            height = 200,
                                        )

                                        Spacer(
                                            modifier = Modifier.height(12.dp),
                                        )

                                        Text("Дата: ${item.createdAt}")

                                        Text(resultText(item))

                                        TextButton(
                                            onClick = {
                                                selectedItem = item
                                            },
                                        ) {
                                            Text("Подробнее")
                                        }
                                    }
                                }
                            }

                            if (current.isLoadingMore) {
                                item {
                                    Box(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }

                            current.loadMoreError?.let { error ->
                                item {
                                    InlineError(
                                        message = error,
                                        onRetry = viewModel::loadNextPage,
                                        retryLabel = "Повторить загрузку",
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Подробности проверки

    selectedItem?.let { item ->

        AlertDialog(
            onDismissRequest = {
                selectedItem = null
            },
            title = {
                Text("Результат проверки")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    HistoryPhoto(
                        storagePath = item.storagePath,
                        viewModel = viewModel,
                        height = 260,
                        onPhotoClick = { bitmap ->
                            fullScreenBitmap = bitmap
                        },
                    )

                    Text("Задание: ${item.taskId}")

                    Text("Дата: ${item.createdAt}")

                    item.checkedAt?.let {
                        Text("Проверено: $it")
                    }

                    Text(resultText(item))

                    item.feedback?.let {
                        Text("Отзыв ИИ: $it")
                    }

                    item.confidence?.let {
                        Text(
                            "Уверенность модели: ${
                                "%.1f".format(it * 100)
                            }%",
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedItem = null
                    },
                ) {
                    Text("Закрыть")
                }
            },
        )
    }

    // Полноэкранная фотография

    fullScreenBitmap?.let { bitmap ->

        FullScreenPhoto(
            bitmap = bitmap,
            onDismiss = {
                fullScreenBitmap = null
            },
        )
    }
}

private fun resultText(item: SubmissionHistoryItem): String =
    when (item.status.lowercase()) {
        "completed" ->
            if (item.passed == true) {
                "Результат: пройдено"
            } else {
                "Результат: не пройдено"
            }

        "queued", "processing" ->
            "Результат: проверяется"

        "failed" ->
            "Результат: ошибка проверки"

        else -> "Статус: ${item.status}"
    }

@Composable
private fun HistoryPhoto(
    storagePath: String,
    viewModel: HistoryViewModel,
    height: Int,
    onPhotoClick: ((Bitmap) -> Unit)? = null,
) {
    var bitmap by remember(storagePath) {
        mutableStateOf<Bitmap?>(null)
    }

    var loading by remember(storagePath) {
        mutableStateOf(true)
    }

    LaunchedEffect(storagePath) {
        loading = true

        bitmap =
            try {
                withContext(Dispatchers.IO) {
                    val photoUrl = viewModel.getPhotoUrl(storagePath)

                    URL(photoUrl).openStream().use {
                        BitmapFactory.decodeStream(it)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "HistoryPhoto",
                    "Не удалось загрузить фото: $storagePath",
                    e,
                )
                null
            }

        loading = false
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clickable(
                    enabled = bitmap != null && onPhotoClick != null,
                ) {
                    bitmap?.let { onPhotoClick?.invoke(it) }
                },
        contentAlignment = Alignment.Center,
    ) {
        when {
            loading -> CircularProgressIndicator()

            bitmap != null -> {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Фотография отправки",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            else -> Text("Фотография недоступна")
        }
    }
}

@Composable
private fun FullScreenPhoto(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
) {
    var scale by remember {
        mutableFloatStateOf(1f)
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clipToBounds(),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Фотография в полном размере",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            bitmap.width.toFloat() / bitmap.height.toFloat(),
                        ).graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }.pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                            }
                        },
                contentScale = ContentScale.Fit,
            )

            TextButton(
                onClick = onDismiss,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
            ) {
                Text("Закрыть")
            }
        }
    }
}
