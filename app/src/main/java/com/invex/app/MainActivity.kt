package com.invex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.invex.app.navigation.InvexApp
import com.invex.app.ui.theme.InvexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InvexTheme {
                // Flow: Onboarding -> Login -> (temporary) Attendance.
                // To preview one screen alone: InvexApp(startDestination = Route.Supplement)
                InvexApp(onExit = { finish() })
            }
        }
    }
}
