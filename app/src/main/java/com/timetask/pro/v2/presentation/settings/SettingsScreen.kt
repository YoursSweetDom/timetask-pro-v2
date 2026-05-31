package com.timetask.pro.v2.presentation.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.timetask.pro.v2.data.preferences.AppPreferences
import com.timetask.pro.v2.ui.theme.AppThemeMode
import com.timetask.pro.v2.ui.theme.Spacing
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val currentMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val longPressDelay by settingsViewModel.longPressDelayMs.collectAsStateWithLifecycle()
    val latchMode by settingsViewModel.latchMode.collectAsStateWithLifecycle()
    val showTriggeredNotification by settingsViewModel.showTriggeredNotification.collectAsStateWithLifecycle()
    
    val backupStatus by settingsViewModel.backupStatus.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { settingsViewModel.importData(it) }
    }

    LaunchedEffect(backupStatus) {
        when (val status = backupStatus) {
            is BackupStatus.Success -> {
                snackbarHostState.showSnackbar(status.message)
                settingsViewModel.resetBackupStatus()
            }
            is BackupStatus.Error -> {
                snackbarHostState.showSnackbar(status.message)
                settingsViewModel.resetBackupStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(Spacing.md))

            // ============================================================
            // Section: Тема оформления
            // ============================================================
            Text(
                text = "Тема оформления",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            ThemeOption(
                icon = Icons.Outlined.DarkMode,
                title = "Titanium Blue",
                subtitle = "Всегда тёмная тема — премиум стиль",
                selected = currentMode == AppThemeMode.TITANIUM_BLUE,
                onClick = { settingsViewModel.setThemeMode(AppThemeMode.TITANIUM_BLUE) },
            )

            Spacer(Modifier.height(Spacing.xs))

            ThemeOption(
                icon = Icons.Outlined.PhoneAndroid,
                title = "Тема устройства",
                subtitle = "Светлая или тёмная — как в системе",
                selected = currentMode == AppThemeMode.SYSTEM,
                onClick = { settingsViewModel.setThemeMode(AppThemeMode.SYSTEM) },
            )

            // ============================================================
            // Section: Навигация
            // ============================================================
            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = "Навигация",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Задержка быстрого меню",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Время удержания кнопки «Инструменты» для вызова быстрого переключателя",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Slider(
                    value = longPressDelay.toFloat(),
                    onValueChange = { value ->
                        settingsViewModel.setLongPressDelayMs(value.roundToInt())
                    },
                    valueRange = AppPreferences.MIN_LONG_PRESS_DELAY_MS.toFloat()..AppPreferences.MAX_LONG_PRESS_DELAY_MS.toFloat(),
                    steps = 3, // (500-100)/100 - 1 = 3 steps (100, 200, 300, 400, 500)
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = "${longPressDelay}мс",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Latch Mode (Залипание)
            Spacer(Modifier.height(Spacing.md))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setLatchMode(!latchMode) }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.TouchApp, // Reusing icon or could be different
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.lg),
                    tint = if (latchMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Залипание меню",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Оставить меню открытым после отпускания кнопки",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = latchMode,
                    onCheckedChange = { settingsViewModel.setLatchMode(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            // ============================================================
            // Section: Уведомления
            // ============================================================
            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = "Уведомления",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { settingsViewModel.setShowTriggeredNotification(!showTriggeredNotification) }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.lg),
                    tint = if (showTriggeredNotification) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Уведомление при срабатывании",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Показывать уведомление в шторке при завершении таймера / будильника",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = showTriggeredNotification,
                    onCheckedChange = { settingsViewModel.setShowTriggeredNotification(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            // ============================================================
            // Section: Резервное копирование
            // ============================================================
            Spacer(Modifier.height(Spacing.lg))

            Text(
                text = "Данные и Резервное копирование",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.sm),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (backupStatus !is BackupStatus.Exporting && backupStatus !is BackupStatus.Importing) {
                            exportLauncher.launch("TimeTaskProBackup.json") 
                        }
                    }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Создать резервную копию",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Экспорт всех задач, таймеров и настроек в JSON",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            
            Spacer(Modifier.height(Spacing.xs))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (backupStatus !is BackupStatus.Exporting && backupStatus !is BackupStatus.Importing) {
                            importLauncher.launch(arrayOf("application/json", "*/*")) 
                        }
                    }
                    .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.SettingsBackupRestore,
                    contentDescription = null,
                    modifier = Modifier.size(Spacing.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Восстановить из копии",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Внимание: текущие данные будут перезаписаны!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            
            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm, horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Spacing.lg),
            tint = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}
