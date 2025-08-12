// MainActivity.kt
package com.example.colfi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.colfi.navigation.NavGraph
import com.example.colfi.ui.theme.ColfiTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        setContent {
            ColfiTheme {
                ColfiApp()
            }
        }
    }
}

@Composable
fun ColfiApp() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}