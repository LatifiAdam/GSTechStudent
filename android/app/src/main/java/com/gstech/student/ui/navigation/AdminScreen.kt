package com.gstech.student.ui.navigation
sealed class AdminScreen(val route:String){
    object Home:AdminScreen("admin/home")
    object Users:AdminScreen("admin/users")
    object Establishments:AdminScreen("admin/establishments")
    object Profile:AdminScreen("admin/profile")
    object Settings:AdminScreen("admin/settings")
    object UserDetail:AdminScreen("admin/users/{userId}"){fun createRoute(userId:String)="admin/users/${android.net.Uri.encode(userId)}"}
    object NewUser:AdminScreen("admin/users/add")
    companion object { val bottomBarRoutes=setOf(Home.route,Users.route,Establishments.route,Profile.route) }
}
