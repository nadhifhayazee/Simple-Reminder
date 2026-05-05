package com.nadhifhayazee.simplereminder.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nadhifhayazee.simplereminder.ui.screen.edit.EditReminderScreen
import com.nadhifhayazee.simplereminder.ui.screen.home.HomeScreen

import androidx.navigation.NavHostController

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Edit : Screen("edit/{reminderId}") {
        fun createRoute(reminderId: Int) = "edit/$reminderId"
    }
}

@Composable
fun ReminderNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController, 
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onEditReminder = { id ->
                    navController.navigate(Screen.Edit.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: return@composable
            EditReminderScreen(
                reminderId = reminderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
