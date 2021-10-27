package com.google.modernstorage.storage

import android.Manifest
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class SharedStorageInstrumentedTest {
    private lateinit var appContext: Context
    private lateinit var fileSystem: SharedFileSystem

    @get:Rule
    val storageAccess = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        fileSystem = SharedFileSystem(appContext)
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.google.modernstorage.storage.test", appContext.packageName)
    }

    @Test
    fun readImageFromSource() {
        val imageUri = MediaStoreUtils.addJpegImage(appContext).also {
            runBlocking {
                MediaStoreUtils.scanUri(appContext, it, "image/jpg")
            }
        }


    }
}
