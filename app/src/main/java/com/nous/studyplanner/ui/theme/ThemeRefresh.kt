package com.nous.studyplanner.ui.theme

import androidx.compose.runtime.mutableStateOf

object ThemeRefresh {
    val state = mutableStateOf(0)
    fun bump() { state.value++ }
}
