package com.example.newsaggregator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.newsaggregator.ui.NewsDetailScreen
import com.example.newsaggregator.ui.NewsListScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object NewsList : Screen("news_list")
    object NewsDetail : Screen("news_detail/{url}") {
        fun createRoute(url: String) = "news_detail/${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.NewsList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.NewsList.route) {
            NewsListScreen(
                onNewsClick = { url ->
                    navController.navigate(Screen.NewsDetail.createRoute(url))
                }
            )
        }
        
        composable(
            route = Screen.NewsDetail.route,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            NewsDetailScreen(
                url = url,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
} 