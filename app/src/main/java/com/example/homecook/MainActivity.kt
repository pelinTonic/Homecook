package com.example.homecook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.homecook.ui.theme.HomeCookTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homecook.navigation.Routes
import com.example.homecook.ui.theme.screens.*
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            HomeCookTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Routes.Splash.route
                ) {

                    composable(Routes.Splash.route) {
                        SplashScreen(navController)
                    }

                    composable(Routes.Login.route) {
                        LoginScreen(navController)
                    }

                    composable(Routes.Register.route) {
                        RegisterScreen(navController)
                    }

                    composable(Routes.Main.route) { MainScreen(navController)

                }
                }
            }
        }
    }
}

