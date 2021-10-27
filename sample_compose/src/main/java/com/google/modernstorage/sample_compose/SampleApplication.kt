package com.google.modernstorage.sample_compose

import android.app.Application
import logcat.AndroidLogcatLogger
import logcat.LogPriority.VERBOSE

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Log all priorities in debug builds, no-op in release builds.
        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)
    }
}
