package com.frq.ytmusic.presentation.navigation

/**
 * Sealed class representing app screens for navigation.
 */
sealed class Screen(val route: String) {
    data object Search : Screen("search")
    data object Library : Screen("library")
    data object Downloads : Screen("downloads")
}
