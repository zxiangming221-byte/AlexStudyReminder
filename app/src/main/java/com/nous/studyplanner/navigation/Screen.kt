package com.nous.studyplanner.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreatePlan : Screen("create_plan")
    data object PlanDetail : Screen("plan_detail/{planId}") {
        fun createRoute(planId: Long) = "plan_detail/$planId"
    }
    data object Settings : Screen("settings")
}
