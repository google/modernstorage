package com.google.modernstorage.storage

import android.os.Build
import android.util.Log
import androidx.core.os.BuildCompat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class PhotoPickerUtilsInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.google.modernstorage.storage.test", appContext.packageName)
    }

    @Test
    fun verifyIfPhotoPickerIsPresent() {
        if (BuildCompat.isAtLeastT()) {
            assertEquals(isPhotoPickerAvailable(), true)
        } else {
            assertEquals(isPhotoPickerAvailable(), false)
        }
    }
}
