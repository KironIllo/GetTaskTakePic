package com.kironillo.presentation.ui.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kironillo.presentation.ui.components.AppCard
import com.kironillo.presentation.viewmodel.SettingsViewModel

private val themeOptions = listOf("SYSTEM" to "Системная", "LIGHT" to "Светлая", "DARK" to "Тёмная")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Диалоги
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showSystemLocationDialog by remember { mutableStateOf(false) }

    // Launcher запроса разрешения
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                if (viewModel.canUseGeolocation()) {
                    viewModel.setGeolocationEnabled(true)
                } else {
                    showSystemLocationDialog = true
                }
            } else {
                showPermissionRationale = true
            }
        }

    // Обновляем статус при возврате в приложение (после системных настроек)
    var refreshTick by remember { mutableStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) refreshTick++
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Пересчитываем подсказку при каждом возврате
    val geolocationWarning =
        remember(refreshTick, state.geolocationEnabled) {
            if (state.geolocationEnabled) viewModel.geolocationUnavailableReason() else null
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Column(Modifier.fillMaxSize().padding(padding), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(Modifier.padding(24.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppCard {
                    SettingSwitch(
                        title = "Геолокация",
                        description =
                            geolocationWarning
                                ?: "Для сохранения картинок на карте",
                        checked = state.geolocationEnabled,
                        onCheckedChange = { checked ->
                            if (!checked) {
                                viewModel.setGeolocationEnabled(false)
                                return@SettingSwitch
                            }
                            // Пытаемся включить — проверяем реальное состояние
                            val permission =
                                ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                ) == PackageManager.PERMISSION_GRANTED

                            when {
                                !permission ->
                                    locationPermissionLauncher.launch(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    )
                                !viewModel.canUseGeolocation() -> showSystemLocationDialog = true
                                else -> viewModel.setGeolocationEnabled(true)
                            }
                        },
                    )
                }
                AppCard {
                    Row(
                        Modifier.fillMaxWidth().clickable { onNavigateToPermissions() }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Разрешения", style = MaterialTheme.typography.titleMedium)
                            Text("Управление доступом к камере и геолокации", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("›", style = MaterialTheme.typography.titleLarge)
                    }
                }
                AppCard {
                    SettingSwitch(
                        "Уведомления",
                        "Сохранить предпочтение уведомлений",
                        state.notificationsEnabled,
                        viewModel::setNotificationsEnabled,
                    )
                }
                AppCard {
                    var expanded by remember { mutableStateOf(false) }
                    val label = themeOptions.firstOrNull { it.first == state.themeMode }?.second ?: "Системная"
                    Column {
                        Text("Тема", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Row(
                            Modifier.fillMaxWidth().clickable { expanded = true }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("Изменить", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            themeOptions.forEach { (value, title) ->
                                DropdownMenuItem(
                                    text = { Text(title) },
                                    onClick = {
                                        viewModel.setThemeMode(value)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                AppCard {
                    Text("О приложении", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("GeTaTaPi — приложение для фото-заданий и проверки результатов.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    // Диалог: разрешение отклонено
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Нужен доступ к местоположению") },
            text = { Text("Разрешите доступ к геолокации, чтобы сохранять фотографии на карте.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionRationale = false
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                }) { Text("Открыть настройки") }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) { Text("Отмена") }
            },
        )
    }

    // Диалог: GPS выключен в системе
    if (showSystemLocationDialog) {
        AlertDialog(
            onDismissRequest = { showSystemLocationDialog = false },
            title = { Text("Геолокация выключена") },
            text = { Text("Включите службы геолокации в настройках телефона, чтобы сохранять фотографии на карте.") },
            confirmButton = {
                TextButton(onClick = {
                    showSystemLocationDialog = false
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }) { Text("Открыть настройки") }
            },
            dismissButton = {
                TextButton(onClick = { showSystemLocationDialog = false }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
