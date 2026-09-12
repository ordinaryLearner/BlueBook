package com.czcz.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.Api.UserViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.Screen.AvatarScreen
import com.czcz.myapp.Screen.BackgroundScreen
import com.czcz.myapp.Screen.ChatListScreen
import com.czcz.myapp.Screen.ChatScreen
import com.czcz.myapp.Screen.DetailScreen
import com.czcz.myapp.Screen.FindScreen
import com.czcz.myapp.Screen.FlashScreen
import com.czcz.myapp.Screen.HistoryPostScreen
import com.czcz.myapp.Screen.HomeScreen
import com.czcz.myapp.Screen.ImageViewScreen
import com.czcz.myapp.Screen.LoginScreen
import com.czcz.myapp.Screen.MineScreen
import com.czcz.myapp.Screen.OpenScreen
import com.czcz.myapp.Screen.PostListScreen
import com.czcz.myapp.Screen.ImagePublishScreen
import com.czcz.myapp.Screen.RegistrationScreen
import com.czcz.myapp.Screen.SettingScreen
import com.czcz.myapp.Screen.UpdateProfileScreen
import com.czcz.myapp.Screen.UserDetailScreen
import com.czcz.myapp.Screen.VideoDetailScreen
import com.czcz.myapp.Screen.VideoPublishScreen
import com.czcz.myapp.Screen.VideoViewScreen

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
    val postViewModel: PostViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()
    val infoViewModel: InfoViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val messageViewModel: MessageViewModel = viewModel()
    NavHost(navController = navController, startDestination = "OpenScreen"){
        composable("OpenScreen"){
            OpenScreen(loginViewModel, navController, postViewModel)
        }
        composable("LoginScreen"){
            LoginScreen(navController, postViewModel,infoViewModel,loginViewModel)
        }
        composable("RegisterScreen"){
            RegistrationScreen(navController)
        }
        composable("FlashScreen"){
            FlashScreen(navController, postViewModel, loginViewModel)
        }
        composable("MineScreen"){
            MineScreen(navController, postViewModel, loginViewModel, infoViewModel)
        }
        composable("HomeScreen"){
            HomeScreen(navController, postViewModel, loginViewModel, infoViewModel, userViewModel, messageViewModel)
        }
        composable("ImagePublishScreen"){
            ImagePublishScreen(navController, postViewModel)
        }
        composable("VideoPublishScreen"){
            VideoPublishScreen(navController, postViewModel)
        }
        composable("ImageViewScreen"){
            ImageViewScreen(navController, postViewModel)
        }
        composable("VideoViewScreen"){
            VideoViewScreen(navController, postViewModel)
        }
        composable("BackgroundScreen"){
            BackgroundScreen(navController, infoViewModel)
        }
        composable("ChatListScreen"){
            ChatListScreen(navController, messageViewModel)
        }
        composable("ChatScreen"){
            ChatScreen(navController, messageViewModel, infoViewModel, postViewModel, loginViewModel, userViewModel)
        }
        composable("AvatarScreen"){
            AvatarScreen(navController, infoViewModel)
        }
        composable("SettingScreen"){
            SettingScreen(navController, loginViewModel, infoViewModel, postViewModel, messageViewModel)
        }
        composable("UserDetailScreen"){
            UserDetailScreen(navController,  loginViewModel, postViewModel, infoViewModel, messageViewModel)
        }
        composable("PostListScreen/{title}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType }
            ),
        ) {
            PostListScreen(it.arguments?.getString("title") ?: "", postViewModel, navController, loginViewModel)
        }
        composable("HistoryPostScreen"){
            HistoryPostScreen(postViewModel, navController, loginViewModel,)
        }
        composable("UpdateProfileScreen"){
            UpdateProfileScreen(infoViewModel, navController)
        }
        composable("FindScreen"){
            FindScreen(navController, postViewModel, userViewModel, infoViewModel,loginViewModel)
        }
        composable("DetailScreen/{isLiked}/{ifFollowed}/{ifStarred}",
            arguments = listOf(
                navArgument("isLiked") { type = NavType.BoolType } ,
                navArgument("ifFollowed") { type = NavType.BoolType },
                navArgument("ifStarred") { type = NavType.BoolType }
            ),
        ) {
            val isLiked = it.arguments?.getBoolean("isLiked") ?: false
            val ifFollowed = it.arguments?.getBoolean("ifFollowed") ?: false
            val ifStarred = it.arguments?.getBoolean("ifStarred") ?: false

            DetailScreen(navController, postViewModel, isLiked,ifStarred,userViewModel,ifFollowed,loginViewModel, infoViewModel)
        }
        composable("VideoDetailScreen/{isLiked}/{ifFollowed}/{ifStarred}",
            arguments = listOf(
                navArgument("isLiked") { type = NavType.BoolType } ,
                navArgument("ifFollowed") { type = NavType.BoolType },
                navArgument("ifStarred") { type = NavType.BoolType }
            ),
        ) {
            val isLiked = it.arguments?.getBoolean("isLiked") ?: false
            val ifFollowed = it.arguments?.getBoolean("ifFollowed") ?: false
            val ifStarred = it.arguments?.getBoolean("ifStarred") ?: false

            VideoDetailScreen(navController, postViewModel,userViewModel, infoViewModel,loginViewModel,isLiked,ifStarred,ifFollowed)
        }
    }
}
