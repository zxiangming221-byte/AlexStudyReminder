package com.nous.studyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nous.studyplanner.navigation.NavGraph
import com.nous.studyplanner.ui.theme.StudyPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyPlannerTheme {
                NavGraph()
            }
        }
    }
}
