package com.czcz.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.czcz.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Main()
        }
    }
}



@Composable
fun Main() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "LoginScreen"){
        composable("LoginScreen"){
            LoginScreen(navController)
        }
        composable("RegisterScreen"){
            RegistrationScreen(navController)
        }
        composable("FlashScreen"){
            FlashScreen(navController)
        }
    }
}
