package com.google.modernstorage.sample_compose

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@ExperimentalMaterialApi
@Composable
fun StorageScreen(viewModel: StorageViewModel = viewModel()) {
    val context = LocalContext.current

}
