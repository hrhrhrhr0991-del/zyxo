package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.gallery.GalleryScreen
import com.example.ui.screens.gallery.GalleryViewModel
import com.example.ui.screens.image.ImageStudioScreen
import com.example.ui.screens.image.ImageStudioViewModel
import com.example.ui.screens.intelligence.IntelligenceScreen
import com.example.ui.screens.intelligence.IntelligenceViewModel
import com.example.ui.screens.video.VideoStudioScreen
import com.example.ui.screens.video.VideoStudioViewModel
import com.example.ui.theme.CyanSpark

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Chat.route

    // Scoped ViewModels
    val chatViewModel: ChatViewModel = viewModel()
    val imageStudioViewModel: ImageStudioViewModel = viewModel()
    val videoStudioViewModel: VideoStudioViewModel = viewModel()
    val intelligenceViewModel: IntelligenceViewModel = viewModel()
    val galleryViewModel: GalleryViewModel = viewModel()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp
            ) {
                NAV_ITEMS.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanSpark,
                            selectedTextColor = CyanSpark,
                            indicatorColor = CyanSpark.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = chatViewModel)
            }
            composable(Screen.ImageStudio.route) {
                ImageStudioScreen(viewModel = imageStudioViewModel)
            }
            composable(Screen.VideoStudio.route) {
                VideoStudioScreen(viewModel = videoStudioViewModel)
            }
            composable(Screen.Intelligence.route) {
                IntelligenceScreen(viewModel = intelligenceViewModel)
            }
            composable(Screen.Gallery.route) {
                GalleryScreen(
                    viewModel = galleryViewModel,
                    onUsePromptInChat = { prompt ->
                        chatViewModel.sendMessage(prompt)
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
