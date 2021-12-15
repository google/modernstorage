/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.modernstorage.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.modernstorage.sample.photopicker.PickVisualMediaScreen
import com.google.modernstorage.sample.saf.SelectDocumentFileScreen
import com.google.modernstorage.sample.ui.theme.ModernStorageTheme

class MainActivity : ComponentActivity() {
    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernStorageTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute
                ) {
                    composable(HomeRoute) {
                        HomeScreen(navController)
                    }

                    /**
                     * MediaStore demos
                     */
                    composable(Demos.AddMediaFile.route) {
                        NotAvailableYetScreen(navController)
                    }
                    composable(Demos.CaptureMediaFile.route) {
                        NotAvailableYetScreen(navController)
                    }
                    composable(Demos.AddFileToDownloads.route) {
                        NotAvailableYetScreen(navController)
                    }
                    composable(Demos.EditMediaFile.route) {
                        NotAvailableYetScreen(navController)
                    }
                    composable(Demos.DeleteMediaFile.route) {
                        NotAvailableYetScreen(navController)
                    }
                    composable(Demos.ListMediaFiles.route) {
                        NotAvailableYetScreen(navController)
                    }

                    /**
                     * Photo Picker demos
                     */
                    composable(Demos.PickVisualMedia.route) {
                        PickVisualMediaScreen(navController)
                    }

                    /**
                     * Storage Access Framework demos
                     */
                    composable(Demos.SelectDocumentFile.route) {
                        SelectDocumentFileScreen(navController)
                    }
                    composable(Demos.EditDocumentFile.route) {
                        NotAvailableYetScreen(navController)
                    }
                }
            }
        }
    }
}
