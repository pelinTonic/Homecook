package com.example.homecook.navigation

sealed class Routes(val route: String) {

    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object Main : Routes("main")

}
