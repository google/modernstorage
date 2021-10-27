package com.google.modernstorage.storage

import android.Manifest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class SharedStorageInstrumentedTest {

    @get:Rule
    val storageAccess = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.google.modernstorage.storage.test", appContext.packageName)
    }

    @Test
    fun readImage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val imageUri = MediaStoreUtils.addJpegImage(appContext)
        runBlocking {
            MediaStoreUtils.scanUri(appContext, imageUri, "image/jpg")
        }


    }
}
