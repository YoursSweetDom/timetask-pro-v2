package com.timetask.pro.v2

import android.app.Application
import android.os.StrictMode

class TimeTaskApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // StrictMode в debug — раннее обнаружение проблем с I/O на main thread
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }
}
