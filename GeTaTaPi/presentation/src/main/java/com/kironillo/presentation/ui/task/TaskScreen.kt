package com.kironillo.presentation.ui.task

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.presentation.ui.components.AppCard
import com.kironillo.presentation.ui.components.PrimaryButton
import com.kironillo.presentation.ui.components.SecondaryButton
import java.io.File
import java.io.FileOutputStream

@Composable
fun TaskScreen(
    task: Task,
    onBack: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var photoError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap ->
            if (bitmap != null) {
                runCatching {
                    val photoFile =
                        File.createTempFile(
                            "task_photo_",
                            ".jpg",
                            context.cacheDir,
                        )
                    FileOutputStream(photoFile).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                    }
                    Uri.fromFile(photoFile)
                }.onSuccess(onPhotoSelected)
                    .onFailure {
                        photoError = it.message ?: "Не удалось сохранить фотографию"
                    }
            }
        }

    var showCameraRationale by remember { mutableStateOf(false) }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                cameraLauncher.launch(null)
            } else {
                showCameraRationale = true
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let(onPhotoSelected)
        }

    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            TaskScreenHeader(onBack = onBack)

            AppCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(top = 48.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Тема: ${task.topic}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Сложность: ${task.difficulty.localizedName()}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Текст задания: ${task.targetObject}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Сделать фото",
                onClick = {
                    val hasPermission =
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA,
                        ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
            )

            SecondaryButton(
                text = "Выбрать из галереи",
                onClick = { galleryLauncher.launch("image/*") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(top = 16.dp),
            )
        }
    }

    photoError?.let { message ->
        AlertDialog(
            onDismissRequest = { photoError = null },
            title = { Text("Неудача") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { photoError = null }) {
                    Text("Понятно")
                }
            },
        )
        if (showCameraRationale) {
            AlertDialog(
                onDismissRequest = { showCameraRationale = false },
                title = { Text("Разрешите доступ к камере") },
                text = { Text("Для того, чтобы делать фотографии, нужна камера, прикинь") },
                confirmButton = {
                    TextButton(onClick = {
                        showCameraRationale = false
                        // Открываем настройки, если отклонили
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        context.startActivity(intent)
                    }) { Text("В настройки") }
                },
                dismissButton = {
                    TextButton(onClick = { showCameraRationale = false }) { Text("Отмена") }
                },
            )
        }
    }
}

@Composable
internal fun TaskScreenHeader(
    onBack: () -> Unit,
    title: String = "Задание",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onBack,
            modifier = Modifier.width(48.dp),
        ) {
            Text("‹", style = MaterialTheme.typography.headlineLarge)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.width(48.dp))
    }
}

private fun TaskDifficulty.localizedName(): String =
    when (this) {
        TaskDifficulty.EASY -> "Лёгкая"
        TaskDifficulty.MEDIUM -> "Средняя"
        TaskDifficulty.HARD -> "Сложная"
    }
