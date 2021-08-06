package com.google.modernstorage.sample_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@ExperimentalMaterialApi
@Composable
fun DemoListScreen(navController: NavController, list: List<Demo>) {
    LazyColumn {
        items(list) { demo ->
            DemoItem(navController, demo)
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun DemoItem(navController: NavController, demo: Demo) {
    ListItem(
        modifier = Modifier.clickable { navController.navigate(demo.route) },
        text = { Text(demo.title) },
        trailing = { Text("MediaStore") },
        icon = { Icon(demo.icon, contentDescription = null) }
    )
    Divider()
}
