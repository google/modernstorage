package com.google.modernstorage.sample_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.modernstorage.sample_compose.mediastore.AddMediaScreen
import com.google.modernstorage.sample_compose.ui.theme.ModernStorageTheme

@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ModernStorageTheme {
                val navController = rememberNavController()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("ModernStorage Demo") }
                        )
                    },
                    content = { innerPadding ->
                        Column(modifier = Modifier.padding(innerPadding)) {
                            NavHost(
                                modifier = Modifier.weight(1f),
                                navController = navController,
                                startDestination = "home"
                            ) {
                                composable("home") { DemoListScreen(navController, demoList) }
                                composable(Demo.AddMedia.route) { AddMediaScreen() }
                            }
                        }

                    }
                )
            }
        }
    }
}

sealed class Demo(val route: String, val icon: ImageVector, val title: String) {
    object AddMedia : Demo("add-media", Icons.Filled.AddAPhoto, "Add Media")
    object DeleteMedia : Demo("delete-media", Icons.Filled.NoPhotography, "Delete Media")
}

val demoList = listOf(
    Demo.AddMedia,
    Demo.DeleteMedia
)
