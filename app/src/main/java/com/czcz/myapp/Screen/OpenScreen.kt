package com.czcz.myapp.Screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.R
import kotlinx.coroutines.delay

@Composable
fun OpenScreen(loginViewModel: LoginViewModel,navController: NavController, postViewModel: PostViewModel) {
    val context = LocalContext.current
    val isLoginSuccess by loginViewModel.loginSuccess.collectAsState()
    LaunchedEffect(null) {
        loginViewModel.autoLogin(context)
        delay(2000)
        if(!isLoginSuccess) navController.navigate("LoginScreen")
        else {
            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
            postViewModel.updatePost(context)
            postViewModel.getVideoPost(context)
            navController.navigate("HomeScreen")
        }
    }
    Scaffold() {
        Box(modifier = Modifier
            .padding(it)
            .fillMaxSize()){
            AsyncImage(
                model = R.drawable.open_flash_image,
                contentDescription = "Open Flash Image",
                contentScale = ContentScale.Crop
            )
        }
    }
}