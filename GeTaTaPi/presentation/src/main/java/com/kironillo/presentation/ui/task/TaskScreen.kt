package com.kironillo.presentation.ui.task

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.presentation.ui.components.AppCard
import com.kironillo.presentation.ui.components.PrimaryButton
import com.kironillo.presentation.ui.components.SecondaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun TaskScreen(
    task: Task,
    onBack: () -> Unit,
    onPhotoSelected: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var cameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
        ) { bitmap ->
            if (bitmap != null) {
                cameraBitmap = bitmap
                selectedImageUri = null

                val photoFile =
                    File.createTempFile(
                        "task_photo_",
                        ".jpg",
                        context.cacheDir,
                    )

                FileOutputStream(photoFile).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }

                onPhotoSelected(Uri.fromFile(photoFile))
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                cameraBitmap = null
                onPhotoSelected(uri)
            }
        }

    val galleryBitmap by produceState<Bitmap?>(
        initialValue = null,
        key1 = selectedImageUri,
    ) {
        value =
            selectedImageUri?.let { uri ->
                withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }.getOrNull()
                }
            }
    }

    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 28.dp, vertical = 16.dp),
        ) {
            TaskHeader(onBack = onBack)

            AppCard(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(top = 48.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(task.topic, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Сложность: ${task.difficulty.localizedName()}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(task.targetObject, style = MaterialTheme.typography.bodyLarge)
                }
            }

            val previewBitmap = cameraBitmap ?: galleryBitmap

            if (previewBitmap != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Выбранное фото", style = MaterialTheme.typography.titleMedium)
                Image(
                    bitmap = previewBitmap.asImageBitmap(),
                    contentDescription = "Предпросмотр фотографии",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(top = 8.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Сделать фото",
                onClick = { cameraLauncher.launch(null) },
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
}

@Composable
private fun TaskHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onBack) {
            Text("‹", style = MaterialTheme.typography.headlineLarge)
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Задание",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 12.dp),
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
