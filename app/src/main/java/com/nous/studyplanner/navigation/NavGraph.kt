package com.nous.studyplanner.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nous.studyplanner.ui.screen.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onCreatePlan = { navController.navigate("create") },
                onPlanClick = { id -> navController.navigate("detail/$id") },
                onSettings = { navController.navigate("settings") })
        }
        composable("create") {
            CreatePlanScreen(onPlanCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() })
        }
        composable("detail/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.LongType })) { entry ->
            PlanDetailScreen(planId = entry.arguments?.getLong("planId") ?: 0L,
                onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
