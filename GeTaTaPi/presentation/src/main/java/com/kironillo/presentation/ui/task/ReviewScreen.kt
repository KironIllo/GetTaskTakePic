package com.kironillo.presentation.ui.task

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kironillo.presentation.ui.components.PrimaryButton
import com.kironillo.presentation.ui.components.SecondaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReviewScreen(
    photoUri: Uri,
    onBack: () -> Unit,
    onRetake: () -> Unit,
    onChooseAnother: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = photoUri,
    ) {
        value =
            withContext(Dispatchers.IO) {
                runCatching {
                    if (photoUri.scheme == "file") {
                        BitmapFactory.decodeFile(photoUri.path)
                    } else {
                        context.contentResolver.openInputStream(photoUri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                }.getOrNull()
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) {
                    Text("‹", style = MaterialTheme.typography.headlineLarge)
                }
                Text(
                    "Проверка задания",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Проверь фотографию перед отправкой",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "Фотография для проверки",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Не удалось загрузить фотографию")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Отправить на проверку",
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "Отправка на сервер будет подключена следующим этапом",
                        )
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
            )

            SecondaryButton(
                text = "Переснять",
                onClick = onRetake,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 12.dp),
            )

            SecondaryButton(
                text = "Выбрать другое фото",
                onClick = onChooseAnother,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 12.dp),
            )
        }
    }
}
