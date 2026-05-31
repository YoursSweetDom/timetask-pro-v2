package com.timetask.pro.v2.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timetask.pro.v2.data.preferences.AppPreferences
import com.timetask.pro.v2.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import com.timetask.pro.v2.data.backup.BackupRestoreManager
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase

/**
 * ViewModel для управления всеми настройками.
 * Использует AndroidViewModel для доступа к Application context.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences.getInstance(application)
    private val database = TimeTaskDatabase.getInstance(application)
    private val backupRestoreManager = BackupRestoreManager(application, database)

    // ============================================================
    // Theme
    // ============================================================

    val themeMode: StateFlow<AppThemeMode> = preferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppThemeMode.TITANIUM_BLUE,
        )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    // ============================================================
    // Navigation — Long Press Delay
    // ============================================================

    val longPressDelayMs: StateFlow<Int> = preferences.longPressDelayMs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppPreferences.DEFAULT_LONG_PRESS_DELAY_MS,
        )

    fun setLongPressDelayMs(delay: Int) {
        viewModelScope.launch {
            preferences.setLongPressDelayMs(delay)
        }
    }

    // ============================================================
    // Navigation — Latch Mode (Залипание меню)
    // ============================================================

    val latchMode: StateFlow<Boolean> = preferences.latchMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    fun setLatchMode(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setLatchMode(enabled)
        }
    }

    // ============================================================
    // Sidebar Quick List
    // ============================================================

    val quickListItems: StateFlow<List<String>> = preferences.quickListItemsJson
        .map { jsonString ->
            try {
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getString(i))
                }
                list
            } catch (e: Exception) {
                listOf("ALL", "TODAY", "TOMORROW", "NEXT_7_DAYS", "INBOX")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = listOf("ALL", "TODAY", "TOMORROW", "NEXT_7_DAYS", "INBOX"),
        )

    fun setQuickListItems(items: List<String>) {
        viewModelScope.launch {
            val jsonArray = JSONArray(items)
            preferences.setQuickListItemsJson(jsonArray.toString())
        }
    }

    // ============================================================
    // Notifications — Triggered Notification Toggle
    // ============================================================

    val showTriggeredNotification: StateFlow<Boolean> = preferences.showTriggeredNotification
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true,
        )

    fun setShowTriggeredNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setShowTriggeredNotification(enabled)
        }
    }

    // ============================================================
    // Backup & Restore (JSON Export / Import)
    // ============================================================

    private val _backupStatus = MutableStateFlow<BackupStatus>(BackupStatus.Idle)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Exporting
            val result = backupRestoreManager.createBackup(uri)
            if (result.isSuccess) {
                _backupStatus.value = BackupStatus.Success("Резервная копия успешно создана!")
            } else {
                _backupStatus.value = BackupStatus.Error(result.exceptionOrNull()?.message ?: "Ошибка при создании резервной копии")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _backupStatus.value = BackupStatus.Importing
            val result = backupRestoreManager.restoreBackup(uri)
            if (result.isSuccess) {
                _backupStatus.value = BackupStatus.Success("Данные успешно восстановлены!")
            } else {
                _backupStatus.value = BackupStatus.Error(result.exceptionOrNull()?.message ?: "Ошибка при восстановлении данных")
            }
        }
    }

    fun resetBackupStatus() {
        _backupStatus.value = BackupStatus.Idle
    }
}

sealed class BackupStatus {
    object Idle : BackupStatus()
    object Exporting : BackupStatus()
    object Importing : BackupStatus()
    data class Success(val message: String) : BackupStatus()
    data class Error(val message: String) : BackupStatus()
}
