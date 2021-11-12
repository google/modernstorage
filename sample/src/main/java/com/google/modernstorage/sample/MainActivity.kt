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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                    startDestination = Demos.SelectDocumentFile.route
                ) {
                    composable(HomeRoute) {
                        HomeScreen(navController)
                    }

                    /**
                     * MediaStore demos
                     */
//                    composable(Demos.AddMediaFile.route) {
//                        AddMediaFileScreen(navController)
//                    }
//                    composable(Demos.CaptureMediaFile.route) {
//                        CaptureMediaFileScreen(navController)
//                    }
//                    composable(Demos.AddFileToDownloads.route) {
//                        AddFileToDownloadsScreen(navController)
//                    }
//                    composable(Demos.EditMediaFile.route) { NotAvailableYetScreen() }
//                    composable(Demos.DeleteMediaFile.route) { NotAvailableYetScreen() }
//                    composable(Demos.ListMediaFiles.route) {
//                        ListMediaFileScreen(navController)
//                    }

                    /**
                     * Storage Access Framework demos
                     */
                    composable(Demos.SelectDocumentFile.route) {
                        SelectDocumentFileScreen(navController)
                    }
                    composable(Demos.CreateDocumentFile.route) { NotAvailableYetScreen() }
                    composable(Demos.EditDocumentFile.route) { NotAvailableYetScreen() }
                }
            }
        }
    }
}

@Composable
fun NotAvailableYetScreen() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.demo_not_available_yet_label))
    }
}
