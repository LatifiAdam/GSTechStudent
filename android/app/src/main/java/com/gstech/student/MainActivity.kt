package com.gstech.student

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import com.gstech.student.ui.navigation.GSTechNavGraph
import com.gstech.student.ui.theme.GSTechStudentTheme
import com.gstech.student.ui.theme.ThemeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as GSTechApp).container

        setContent {
            GSTechStudentTheme(darkTheme = ThemeController.darkTheme) {
                GSTechNavGraph(container = container)
            }
        }
    }
}
