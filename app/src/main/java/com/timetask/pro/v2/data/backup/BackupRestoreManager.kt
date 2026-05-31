package com.timetask.pro.v2.data.backup

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.timetask.pro.v2.data.local.db.TimeTaskDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.io.OutputStreamWriter

import com.timetask.pro.v2.data.preferences.dataStore

/**
 * Менеджер для выгрузки (экспорта) всей базы данных и настроек в JSON файл,
 * а также полной перезаписи (импорта) приложения из JSON.
 */
class BackupRestoreManager(
    private val context: Context,
    private val database: TimeTaskDatabase
) {
    private val contentResolver = context.contentResolver
    private val backupDao = database.backupDao()
    
    // JSON configuration
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true 
        prettyPrint = false
        // Allow using strings for enums, no strict type bounds
    }

    suspend fun createBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Collect DataStore
            val prefsMap = context.dataStore.data.first().asMap()
            val prefsJsonObject = JsonObject(
                prefsMap.mapKeys { it.key.name }.mapValues {
                    val value = it.value
                    when (value) {
                        is String -> JsonPrimitive(value)
                        is Boolean -> JsonPrimitive(value)
                        is Int -> JsonPrimitive(value)
                        is Long -> JsonPrimitive(value)
                        is Float -> JsonPrimitive(value)
                        is Double -> JsonPrimitive(value)
                        else -> JsonPrimitive(value.toString())
                    }
                }
            )

            // 2. Collect DB via BackupDao
            val payload = BackupPayload(
                appPreferencesJson = prefsJsonObject.toString(),
                tasks = backupDao.getAllTasks(),
                folders = backupDao.getAllFolders(),
                tags = backupDao.getAllTags(),
                categories = backupDao.getAllCategories(),
                filters = backupDao.getAllFilters(),
                notes = backupDao.getAllNotes(),
                timers = backupDao.getAllTimers(),
                alarms = backupDao.getAllAlarms(),
                templates = backupDao.getAllTemplates(),
                userPresets = backupDao.getAllUserPresets(),
                stopwatches = backupDao.getAllStopwatches(),
                stopwatchLaps = backupDao.getAllStopwatchLaps()
            )

            // 3. Serialize to JSON string
            val jsonString = json.encodeToString(payload)

            // 4. Write to Uri
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Failed to open output stream for URI"))

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Read JSON from Uri
            val jsonString = contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return@withContext Result.failure(Exception("Failed to open input stream for URI"))

            // 2. Deserialize Payload
            val payload = json.decodeFromString<BackupPayload>(jsonString)

            // 3. Restore DataStore Preferences
            val prefsJsonElement = runCatching { Json.parseToJsonElement(payload.appPreferencesJson) }.getOrNull()
            if (prefsJsonElement is JsonObject) {
                context.dataStore.edit { prefs ->
                    prefs.clear()
                    prefsJsonElement.forEach { (keyName, jsonElement) ->
                        if (jsonElement is JsonPrimitive) {
                            when {
                                jsonElement.isString -> prefs[stringPreferencesKey(keyName)] = jsonElement.content
                                jsonElement.booleanOrNull != null -> prefs[booleanPreferencesKey(keyName)] = jsonElement.boolean
                                jsonElement.intOrNull != null -> prefs[intPreferencesKey(keyName)] = jsonElement.int
                                jsonElement.longOrNull != null -> prefs[longPreferencesKey(keyName)] = jsonElement.long
                                jsonElement.floatOrNull != null -> prefs[floatPreferencesKey(keyName)] = jsonElement.float
                                jsonElement.doubleOrNull != null -> prefs[doublePreferencesKey(keyName)] = jsonElement.double
                            }
                        }
                    }
                }
            }

            // 4. Restore DB Atomically
            database.withTransaction {
                // Delete all existing data
                backupDao.deleteAllStopwatchLaps()
                backupDao.deleteAllStopwatches()
                backupDao.deleteAllUserPresets()
                backupDao.deleteAllTemplates()
                backupDao.deleteAllAlarms()
                backupDao.deleteAllTimers()
                backupDao.deleteAllNotes()
                backupDao.deleteAllFilters()
                backupDao.deleteAllCategories()
                backupDao.deleteAllTags()
                backupDao.deleteAllFolders()
                backupDao.deleteAllTasks()

                // Insert new data
                backupDao.insertTasks(payload.tasks)
                backupDao.insertFolders(payload.folders)
                backupDao.insertTags(payload.tags)
                backupDao.insertCategories(payload.categories)
                backupDao.insertFilters(payload.filters)
                backupDao.insertNotes(payload.notes)
                backupDao.insertTimers(payload.timers)
                backupDao.insertAlarms(payload.alarms)
                backupDao.insertTemplates(payload.templates)
                backupDao.insertUserPresets(payload.userPresets)
                backupDao.insertStopwatches(payload.stopwatches)
                backupDao.insertStopwatchLaps(payload.stopwatchLaps)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
