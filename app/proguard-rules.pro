# ============================================================
# TimeTask Pro V2 — ProGuard / R8 Rules
# ============================================================

# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# Room
# ============================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# ============================================================
# kotlinx.serialization
# ============================================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `@Serializable` classes
-keep,includedescriptorclasses class com.timetask.pro.v2.**$$serializer { *; }
-keepclassmembers class com.timetask.pro.v2.** {
    *** Companion;
}
-keepclasseswithmembers class com.timetask.pro.v2.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================================
# Ktor
# ============================================================
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# ============================================================
# Coil
# ============================================================
-dontwarn coil3.**

# ============================================================
# Coroutines
# ============================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}