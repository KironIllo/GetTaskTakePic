package com.kironillo.presentation.ui.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.presentation.viewmodel.PermissionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    onBack: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var refreshTrigger by rememberSaveable { mutableStateOf(false) }

    // Лаунчер для запроса разрешений
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { _ ->
            refreshTrigger = !refreshTrigger // Обновить UI после ответа
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Разрешения") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Для корректной работы приложению требуются следующие разрешения:", style = MaterialTheme.typography.bodyLarge)

            PermissionItem(
                title = "Камера",
                type = PermissionType.CAMERA,
                viewModel = viewModel,
                refreshTrigger = refreshTrigger,
                onRequest = { permissionLauncher.launch(PermissionType.CAMERA.manifestName) },
                onOpenSettings = {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                },
            )

            PermissionItem(
                title = "Геолокация",
                type = PermissionType.LOCATION,
                viewModel = viewModel,
                refreshTrigger = refreshTrigger,
                onRequest = { permissionLauncher.launch(PermissionType.LOCATION.manifestName) },
                onOpenSettings = {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                },
            )
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    type: PermissionType,
    viewModel: PermissionsViewModel,
    refreshTrigger: Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    // Пересчитываем статус при каждом обновлении refreshTrigger
    val status = remember(refreshTrigger) { viewModel.getStatus(type) }
    val rationale = viewModel.getRationale(type)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(rationale, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text =
                        when (status) {
                            PermissionStatus.GRANTED -> "Разрешено"
                            PermissionStatus.DENIED -> "Отклонено (нужно объяснение)"
                            PermissionStatus.PERMANENTLY_DENIED -> "Запрещено навсегда"
                            PermissionStatus.NOT_REQUESTED -> "Не запрошено"
                        },
                    color = if (status == PermissionStatus.GRANTED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )

                when (status) {
                    PermissionStatus.GRANTED -> { /* Ничего не делаем */ }
                    PermissionStatus.PERMANENTLY_DENIED -> {
                        Button(onClick = onOpenSettings) { Text("Настройки") }
                    }
                    else -> {
                        Button(onClick = onRequest) { Text("Разрешить") }
                    }
                }
            }
        }
    }
}
