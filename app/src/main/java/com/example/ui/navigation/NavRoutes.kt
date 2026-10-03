package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    data object Chat : Screen(
        route = "chat",
        title = "گفتگو",
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline,
        testTag = "nav_chat"
    )

    data object ImageStudio : Screen(
        route = "image_studio",
        title = "تصویر 4K",
        selectedIcon = Icons.Filled.Image,
        unselectedIcon = Icons.Outlined.Image,
        testTag = "nav_image"
    )

    data object VideoStudio : Screen(
        route = "video_studio",
        title = "ویدیو Veo",
        selectedIcon = Icons.Filled.Movie,
        unselectedIcon = Icons.Outlined.Movie,
        testTag = "nav_video"
    )

    data object Intelligence : Screen(
        route = "intelligence",
        title = "هوشمندی",
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "nav_intelligence"
    )

    data object Gallery : Screen(
        route = "gallery",
        title = "گالری و سابقه",
        selectedIcon = Icons.Filled.Collections,
        unselectedIcon = Icons.Outlined.Collections,
        testTag = "nav_gallery"
    )
}

val NAV_ITEMS = listOf(
    Screen.Chat,
    Screen.ImageStudio,
    Screen.VideoStudio,
    Screen.Intelligence,
    Screen.Gallery
)
