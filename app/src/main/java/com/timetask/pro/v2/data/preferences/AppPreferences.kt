package com.timetask.pro.v2.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.timetask.pro.v2.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.timetask.pro.v2.domain.model.tasks.TasksViewMode
import com.timetask.pro.v2.domain.model.tasks.TasksSortOrder
import com.timetask.pro.v2.domain.model.tasks.TasksGroupingOrder
import com.timetask.pro.v2.domain.model.tasks.TasksViewPreferences
import com.timetask.pro.v2.domain.model.tasks.TaskCheckboxColorMode

val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Централизованное хранение настроек приложения через DataStore.
 * Без Hilt — создаётся вручную через Application context.
 */
class AppPreferences(private val context: Context) {

    // ============================================================
    // Theme
    // ============================================================

    private val themeModeKey = stringPreferencesKey("theme_mode")
    
    // ============================================================
    // Sidebar & QuickList Config
    // ============================================================
    
    // JSON arrays storing active/ordered section IDs
    private val sidebarSectionsKey = stringPreferencesKey("sidebar_sections_order")
    private val quickListItemsKey = stringPreferencesKey("quick_list_items")

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[themeModeKey] ?: AppThemeMode.TITANIUM_BLUE.name
        AppThemeMode.valueOf(name)
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }

    // ============================================================
    // Navigation — Long Press Delay для Tools Quick Access
    // ============================================================

    private val longPressDelayKey = intPreferencesKey("long_press_delay_ms")

    val longPressDelayMs: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[longPressDelayKey] ?: DEFAULT_LONG_PRESS_DELAY_MS
    }

    suspend fun setLongPressDelayMs(delay: Int) {
        context.dataStore.edit { prefs ->
            prefs[longPressDelayKey] = delay.coerceIn(
                MIN_LONG_PRESS_DELAY_MS,
                MAX_LONG_PRESS_DELAY_MS,
            )
        }
    }

    // ============================================================
    // Navigation — Latch Mode (Залипание меню)
    // ============================================================

    private val latchModeKey = booleanPreferencesKey("latch_mode")

    val latchMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[latchModeKey] ?: false
    }

    suspend fun setLatchMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[latchModeKey] = enabled
        }
    }

    // ============================================================
    // Notifications — Show Triggered Notification
    // ============================================================

    private val showTriggeredNotificationKey = booleanPreferencesKey("show_triggered_notification")

    /**
     * Показывать ли уведомление в шторке при срабатывании таймера/будильника.
     * true  = IMPORTANCE_HIGH notification с heads-up (default)
     * false = IMPORTANCE_LOW «тихое» notification (без heads-up)
     */
    val showTriggeredNotification: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[showTriggeredNotificationKey] ?: true
    }

    suspend fun setShowTriggeredNotification(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[showTriggeredNotificationKey] = enabled
        }
    }

    // ============================================================
    // Sidebar Accessors
    // ============================================================
    
    val sidebarSectionsJson: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[sidebarSectionsKey] ?: DEFAULT_SIDEBAR_SECTIONS_JSON
    }

    suspend fun setSidebarSectionsJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[sidebarSectionsKey] = json
        }
    }

    val quickListItemsJson: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[quickListItemsKey] ?: DEFAULT_QUICK_LIST_ITEMS_JSON
    }

    suspend fun setQuickListItemsJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[quickListItemsKey] = json
        }
    }

    // ============================================================
    // Tasks List View Settings
    // ============================================================

    private val tasksViewModeKey = stringPreferencesKey("tasks_view_mode")
    private val tasksHideCompletedKey = booleanPreferencesKey("tasks_hide_completed")
    private val tasksHideSubtasksKey = booleanPreferencesKey("tasks_hide_subtasks")
    private val tasksHideDetailsKey = booleanPreferencesKey("tasks_hide_details")
    private val tasksSortOrderKey = stringPreferencesKey("tasks_sort_order")
    private val tasksGroupingOrderKey = stringPreferencesKey("tasks_grouping_order")
    private val tasksCheckboxColorModeKey = stringPreferencesKey("tasks_checkbox_color_mode")

    val tasksViewPreferences: Flow<TasksViewPreferences> = context.dataStore.data.map { prefs ->
        TasksViewPreferences(
            viewMode = runCatching { TasksViewMode.valueOf(prefs[tasksViewModeKey] ?: "") }.getOrDefault(TasksViewMode.LIST),
            hideCompleted = prefs[tasksHideCompletedKey] ?: false,
            hideSubtasks = prefs[tasksHideSubtasksKey] ?: false,
            hideDetails = prefs[tasksHideDetailsKey] ?: false,
            sortOrder = runCatching { TasksSortOrder.valueOf(prefs[tasksSortOrderKey] ?: "") }.getOrDefault(TasksSortOrder.DATE),
            groupingOrder = runCatching { TasksGroupingOrder.valueOf(prefs[tasksGroupingOrderKey] ?: "") }.getOrDefault(TasksGroupingOrder.LIST),
            checkboxColorMode = runCatching { TaskCheckboxColorMode.valueOf(prefs[tasksCheckboxColorModeKey] ?: "") }.getOrDefault(TaskCheckboxColorMode.DEFAULT)
        )
    }

    suspend fun updateTasksViewPreferences(
        viewMode: TasksViewMode? = null,
        hideCompleted: Boolean? = null,
        hideSubtasks: Boolean? = null,
        hideDetails: Boolean? = null,
        sortOrder: TasksSortOrder? = null,
        groupingOrder: TasksGroupingOrder? = null,
        checkboxColorMode: TaskCheckboxColorMode? = null
    ) {
        context.dataStore.edit { prefs ->
            viewMode?.let { prefs[tasksViewModeKey] = it.name }
            hideCompleted?.let { prefs[tasksHideCompletedKey] = it }
            hideSubtasks?.let { prefs[tasksHideSubtasksKey] = it }
            hideDetails?.let { prefs[tasksHideDetailsKey] = it }
            sortOrder?.let { prefs[tasksSortOrderKey] = it.name }
            groupingOrder?.let { prefs[tasksGroupingOrderKey] = it.name }
            checkboxColorMode?.let { prefs[tasksCheckboxColorModeKey] = it.name }
        }
    }

    companion object {
        const val DEFAULT_LONG_PRESS_DELAY_MS = 300
        const val MIN_LONG_PRESS_DELAY_MS = 100
        const val MAX_LONG_PRESS_DELAY_MS = 500
        
        // Default Sidebar (Top to Bottom): Pinned, Subscriptions, Categories, Tags, Filters, Folders
        const val DEFAULT_SIDEBAR_SECTIONS_JSON = "[\"PINNED\", \"SUBSCRIPTIONS\", \"CATEGORIES\", \"TAGS\", \"FILTERS\", \"FOLDERS\"]"
        
        // Default Quick List: All, Today, Tomorrow, Next7Days, Inbox
        const val DEFAULT_QUICK_LIST_ITEMS_JSON = "[\"ALL\", \"TODAY\", \"TOMORROW\", \"NEXT_7_DAYS\", \"INBOX\"]"

        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences {
            return instance ?: synchronized(this) {
                instance ?: AppPreferences(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
