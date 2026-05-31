package com.timetask.pro.v2.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timetask.pro.v2.data.local.db.dao.FolderDao
import com.timetask.pro.v2.data.local.db.dao.NoteDao
import com.timetask.pro.v2.data.local.db.dao.TaskDao
import com.timetask.pro.v2.data.local.db.dao.TimerDao
import com.timetask.pro.v2.data.local.db.dao.UserPresetDao
import com.timetask.pro.v2.data.local.db.dao.TagDao
import com.timetask.pro.v2.data.local.db.dao.CategoryDao
import com.timetask.pro.v2.data.local.db.dao.FilterDao
import com.timetask.pro.v2.data.local.db.entity.FolderEntity
import com.timetask.pro.v2.data.local.db.entity.NoteEntity
import com.timetask.pro.v2.data.local.db.entity.TagEntity
import com.timetask.pro.v2.data.local.db.entity.TaskEntity
import com.timetask.pro.v2.data.local.db.entity.TimerEntity
import com.timetask.pro.v2.data.local.db.entity.UserPresetEntity
import com.timetask.pro.v2.data.local.entity.StopwatchEntity
import com.timetask.pro.v2.data.local.entity.StopwatchLapEntity
import com.timetask.pro.v2.data.local.dao.StopwatchDao
import com.timetask.pro.v2.data.local.db.dao.AlarmDao
import com.timetask.pro.v2.data.local.db.entity.AlarmEntity
import com.timetask.pro.v2.data.local.db.entity.CategoryEntity
import com.timetask.pro.v2.data.local.db.entity.FilterEntity
import com.timetask.pro.v2.data.local.db.dao.TemplateDao
import com.timetask.pro.v2.data.local.db.entity.TemplateEntity
import com.timetask.pro.v2.data.local.db.dao.BackupDao
import com.timetask.pro.v2.data.local.db.entity.TaskTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.TimerTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.AlarmTagCrossRef
import com.timetask.pro.v2.data.local.db.entity.StopwatchTagCrossRef

@Database(
    entities = [
        TaskEntity::class,
        FolderEntity::class,
        TagEntity::class,
        NoteEntity::class,
        TimerEntity::class,
        UserPresetEntity::class,
        StopwatchEntity::class,
        StopwatchLapEntity::class,
        AlarmEntity::class,
        FilterEntity::class,
        TemplateEntity::class,
        CategoryEntity::class,
        TaskTagCrossRef::class,
        TimerTagCrossRef::class,
        AlarmTagCrossRef::class,
        StopwatchTagCrossRef::class,
    ],
    version = 21,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TimeTaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun timerDao(): TimerDao
    abstract fun userPresetDao(): UserPresetDao
    abstract fun stopwatchDao(): StopwatchDao
    abstract fun alarmDao(): AlarmDao
    abstract fun templateDao(): TemplateDao
    abstract fun tagDao(): TagDao
    abstract fun categoryDao(): CategoryDao
    abstract fun filterDao(): FilterDao
    abstract fun backupDao(): BackupDao

    companion object {
        @Volatile
        private var instance: TimeTaskDatabase? = null

        fun getInstance(context: Context): TimeTaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TimeTaskDatabase::class.java,
                    "timetask.db",
                )
                    .addMigrations(
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                    )
                    .build()
                    .also { instance = it }
            }
        }
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns to stopwatches
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN categoryText TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN tagsText TEXT NOT NULL DEFAULT ''")
        
        // Add new columns to stopwatch_laps
        db.execSQL("ALTER TABLE stopwatch_laps ADD COLUMN categoryText TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE stopwatch_laps ADD COLUMN tagsText TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new columns to stopwatches
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN deletedAt INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stopwatches_isDeleted ON stopwatches(isDeleted)")

        // Add new columns to stopwatch_laps
        db.execSQL("ALTER TABLE stopwatch_laps ADD COLUMN colorARGB INTEGER")
        db.execSQL("ALTER TABLE stopwatch_laps ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE stopwatch_laps ADD COLUMN deletedAt INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stopwatch_laps_isDeleted ON stopwatch_laps(isDeleted)")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `alarms` (
                `id` TEXT NOT NULL, 
                `hour` INTEGER NOT NULL, 
                `minute` INTEGER NOT NULL, 
                `repeatDaysMask` INTEGER NOT NULL, 
                `isEnabled` INTEGER NOT NULL, 
                `nextTriggerTime` INTEGER NOT NULL, 
                `label` TEXT NOT NULL, 
                `soundUri` TEXT, 
                `vibrationPattern` TEXT, 
                `volume` REAL, 
                `snoozeDurationMinutes` INTEGER NOT NULL, 
                `deleteAfterGoOff` INTEGER NOT NULL, 
                `isDeleted` INTEGER NOT NULL, 
                `deletedAt` INTEGER, 
                `createdAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE alarms ADD COLUMN snoozeRepeatTimes INTEGER NOT NULL DEFAULT 3")
        db.execSQL("ALTER TABLE alarms ADD COLUMN colorARGB INTEGER")
        db.execSQL("ALTER TABLE alarms ADD COLUMN categoryText TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE alarms ADD COLUMN tagsText TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE alarms ADD COLUMN notesText TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE templates ADD COLUMN folderId INTEGER")
        db.execSQL("ALTER TABLE templates ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE templates ADD COLUMN description TEXT")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // --- Migrate timers ---
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `timers_new` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `totalDurationMs` INTEGER NOT NULL, 
                `remainingMs` INTEGER NOT NULL, 
                `state` TEXT NOT NULL, 
                `endTimeMs` INTEGER NOT NULL, 
                `startedAtMs` INTEGER NOT NULL, 
                `pausedAtMs` INTEGER NOT NULL, 
                `accumulatedPauseMs` INTEGER NOT NULL, 
                `categoryId` TEXT, 
                `tagIdsJson` TEXT NOT NULL, 
                `linkedTaskIdsJson` TEXT NOT NULL, 
                `createdAt` INTEGER NOT NULL, 
                `cfg_adjustStepSec` INTEGER NOT NULL, 
                `cfg_isCountDown` INTEGER NOT NULL, 
                `cfg_enableOvertime` INTEGER NOT NULL, 
                `cfg_autoRepeat` INTEGER NOT NULL, 
                `cfg_autoReset` INTEGER NOT NULL, 
                `cfg_quickAddDurationSec` INTEGER NOT NULL, 
                `notif_showInNotifications` INTEGER NOT NULL, 
                `notif_soundUri` TEXT, 
                `notif_volume` REAL NOT NULL, 
                `notif_isSilent` INTEGER NOT NULL, 
                `notif_isLooping` INTEGER NOT NULL, 
                `notif_vibrationPatternJson` TEXT, 
                `notif_snoozeDurationSec` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO `timers_new` (`id`, `name`, `totalDurationMs`, `remainingMs`, `state`, `endTimeMs`, `startedAtMs`, `pausedAtMs`, `accumulatedPauseMs`, `categoryId`, `tagIdsJson`, `linkedTaskIdsJson`, `createdAt`, `cfg_adjustStepSec`, `cfg_isCountDown`, `cfg_enableOvertime`, `cfg_autoRepeat`, `cfg_autoReset`, `cfg_quickAddDurationSec`, `notif_showInNotifications`, `notif_soundUri`, `notif_volume`, `notif_isSilent`, `notif_isLooping`, `notif_vibrationPatternJson`, `notif_snoozeDurationSec`)
            SELECT `id`, `name`, `totalDurationMs`, `remainingMs`, `state`, `endTimeMs`, `startedAtMs`, `pausedAtMs`, `accumulatedPauseMs`, `categoryId`, `tagIdsJson`, 
                   CASE WHEN `linkedTaskId` IS NOT NULL THEN '[' || CAST(`linkedTaskId` AS TEXT) || ']' ELSE '[]' END, 
                   `createdAt`, `cfg_adjustStepSec`, `cfg_isCountDown`, `cfg_enableOvertime`, `cfg_autoRepeat`, `cfg_autoReset`, `cfg_quickAddDurationSec`, `notif_showInNotifications`, `notif_soundUri`, `notif_volume`, `notif_isSilent`, `notif_isLooping`, `notif_vibrationPatternJson`, `notif_snoozeDurationSec` 
            FROM `timers`
        """.trimIndent())

        db.execSQL("DROP TABLE `timers`")
        db.execSQL("ALTER TABLE `timers_new` RENAME TO `timers`")

        // --- Migrate stopwatches ---
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `stopwatches_new` (
                `id` TEXT NOT NULL, 
                `name` TEXT NOT NULL, 
                `state` TEXT NOT NULL, 
                `startTimeMs` INTEGER NOT NULL, 
                `accumulatedMs` INTEGER NOT NULL, 
                `categoryId` INTEGER, 
                `linkedTaskIdsJson` TEXT NOT NULL, 
                `categoryText` TEXT NOT NULL, 
                `tagsText` TEXT NOT NULL, 
                `isDeleted` INTEGER NOT NULL, 
                `deletedAt` INTEGER, 
                `createdAt` INTEGER NOT NULL, 
                PRIMARY KEY(`id`)
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO `stopwatches_new` (`id`, `name`, `state`, `startTimeMs`, `accumulatedMs`, `categoryId`, `linkedTaskIdsJson`, `categoryText`, `tagsText`, `isDeleted`, `deletedAt`, `createdAt`)
            SELECT `id`, `name`, `state`, `startTimeMs`, `accumulatedMs`, `categoryId`, 
                   CASE WHEN `linkedTaskId` IS NOT NULL THEN '[' || CAST(`linkedTaskId` AS TEXT) || ']' ELSE '[]' END, 
                   `categoryText`, `tagsText`, `isDeleted`, `deletedAt`, `createdAt` 
            FROM `stopwatches`
        """.trimIndent())

        db.execSQL("DROP TABLE `stopwatches`")
        db.execSQL("ALTER TABLE `stopwatches_new` RENAME TO `stopwatches`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stopwatches_isDeleted` ON `stopwatches` (`isDeleted`)")
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Alarms: add ringingDurationSec and autoSnoozeDurationSec
        db.execSQL("ALTER TABLE alarms ADD COLUMN ringingDurationSec INTEGER NOT NULL DEFAULT 300")
        db.execSQL("ALTER TABLE alarms ADD COLUMN autoSnoozeDurationSec INTEGER NOT NULL DEFAULT 300")
        
        // Timers: add notif_ringingDurationSec
        db.execSQL("ALTER TABLE timers ADD COLUMN notif_ringingDurationSec INTEGER NOT NULL DEFAULT 180")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create CrossRef Tables
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `task_tag_cross_ref` (
                `taskId` INTEGER NOT NULL, 
                `tagId` INTEGER NOT NULL, 
                PRIMARY KEY(`taskId`, `tagId`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_tag_cross_ref_taskId` ON `task_tag_cross_ref` (`taskId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_tag_cross_ref_tagId` ON `task_tag_cross_ref` (`tagId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `timer_tag_cross_ref` (
                `timerId` TEXT NOT NULL, 
                `tagId` INTEGER NOT NULL, 
                PRIMARY KEY(`timerId`, `tagId`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_timer_tag_cross_ref_timerId` ON `timer_tag_cross_ref` (`timerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_timer_tag_cross_ref_tagId` ON `timer_tag_cross_ref` (`tagId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `alarm_tag_cross_ref` (
                `alarmId` TEXT NOT NULL, 
                `tagId` INTEGER NOT NULL, 
                PRIMARY KEY(`alarmId`, `tagId`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alarm_tag_cross_ref_alarmId` ON `alarm_tag_cross_ref` (`alarmId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_alarm_tag_cross_ref_tagId` ON `alarm_tag_cross_ref` (`tagId`)")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `stopwatch_tag_cross_ref` (
                `stopwatchId` TEXT NOT NULL, 
                `tagId` INTEGER NOT NULL, 
                PRIMARY KEY(`stopwatchId`, `tagId`)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stopwatch_tag_cross_ref_stopwatchId` ON `stopwatch_tag_cross_ref` (`stopwatchId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stopwatch_tag_cross_ref_tagId` ON `stopwatch_tag_cross_ref` (`tagId`)")
    }
}

// ============================================================
// Миграции, заполняющие пробелы (ранее покрывались fallbackToDestructiveMigration)
// ============================================================

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Новые таблицы: filters, templates
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `filters` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT,
                `color` TEXT,
                `order` INTEGER NOT NULL,
                `isPinned` INTEGER NOT NULL,
                `logicJson` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `templates` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT,
                `order` INTEGER NOT NULL,
                `taskConfigJson` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        // Новые колонки в stopwatches
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN categoryId INTEGER")
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN linkedTaskId INTEGER")

        // Новая колонка в timers
        db.execSQL("ALTER TABLE timers ADD COLUMN linkedTaskId INTEGER")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Новая таблица: categories
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT,
                `color` TEXT,
                `order` INTEGER NOT NULL,
                `parentId` INTEGER,
                `isHidden` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL
            )
        """.trimIndent())

        // Новые колонки в tasks
        db.execSQL("ALTER TABLE tasks ADD COLUMN totalSpentTimeMs INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN progressPercent INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN categoryId INTEGER")
        db.execSQL("ALTER TABLE tasks ADD COLUMN pinMode INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_16_18 = object : Migration(16, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Stopwatches: embedded notification field
        db.execSQL("ALTER TABLE stopwatches ADD COLUMN notif_showInNotifications INTEGER NOT NULL DEFAULT 1")

        // Alarms: tagIdsJson
        db.execSQL("ALTER TABLE alarms ADD COLUMN tagIdsJson TEXT NOT NULL DEFAULT '[]'")

        // Templates: emoji + tagIdsJson
        db.execSQL("ALTER TABLE templates ADD COLUMN emoji TEXT")
        db.execSQL("ALTER TABLE templates ADD COLUMN tagIdsJson TEXT NOT NULL DEFAULT '[]'")

        // Filters: emoji
        db.execSQL("ALTER TABLE filters ADD COLUMN emoji TEXT")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No-op: схемы v18 и v19 идентичны
    }
}
