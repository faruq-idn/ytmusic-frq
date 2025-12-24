package com.frq.ytmusic.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val nestedRoutes: List<String> = emptyList() // Routes that belong to this tab
)

val bottomNavItems = listOf(
    BottomNavItem(
        Screen.Search, 
        "Beranda", 
        Icons.Filled.Home, 
        Icons.Outlined.Home,
        nestedRoutes = listOf("ytm_playlist/{playlistId}", "album/{browseId}")
    ),
    BottomNavItem(
        Screen.Collection, 
        "Koleksi", 
        Icons.Filled.LibraryMusic, 
        Icons.Outlined.LibraryMusic,
        nestedRoutes = listOf("downloads", "liked_songs", "playlist/{playlistId}")
    )
)

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        bottomNavItems.forEach { item ->
            // Check if current route is this tab OR a nested route of this tab
            val isOnThisTab = currentRoute == item.screen.route
            val isOnNestedRoute = item.nestedRoutes.any { nested ->
                currentRoute?.startsWith(nested.substringBefore("{")) == true ||
                currentRoute == nested
            }
            val selected = isOnThisTab || isOnNestedRoute
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (isOnNestedRoute) {
                        // Pop back to the tab's root screen
                        navController.popBackStack(item.screen.route, inclusive = false)
                    } else if (!isOnThisTab) {
                        // Navigate to this tab
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                    // If already on this tab's root, do nothing
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
