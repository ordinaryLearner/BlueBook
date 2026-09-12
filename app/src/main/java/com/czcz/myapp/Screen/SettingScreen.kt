package com.czcz.myapp.Screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.DataStorePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController, loginViewModel: LoginViewModel, infoViewModel: InfoViewModel, postViewModel: PostViewModel, messageViewModel: MessageViewModel) {
    val ifQuit = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val user = DataStorePreference.getUser(context).collectAsState(User.empty())
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.Center){
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("设置")
                    }
                },
                navigationIcon = {
                    Icon(Icons.Filled.ArrowBack, "返回", modifier = Modifier.clickable { navController.popBackStack() })
                }
            )
                 },
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)){
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {
                    infoViewModel.setNewUsername(user.value.username ?: "")
                    infoViewModel.setNewBio(user.value.bio ?: "")
                    infoViewModel.setIsUpdateProfile(true)
                    navController.navigate("UpdateProfileScreen")
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("个人信息更新",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {
                    navController.navigate("HistoryPostScreen")
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("浏览记录",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.height(30.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {
                    navController.navigate("PostListScreen/${"我的收藏"}")
                    postViewModel.setPostType(PostType.FAVOURITE)
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("我的收藏",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {navController.navigate("PostListScreen/${"点赞作品"}")
                    postViewModel.setPostType(PostType.LIKE)},
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("我的点赞",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {navController.navigate("PostListScreen/${"我的作品"}")
                    postViewModel.setPostType(PostType.MINE)},
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("我的作品",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
            }
            Spacer(modifier = Modifier.height(50.dp))
            Row(modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp)
                .padding(8.dp)
                .background(Color.White)
                .clickable {
                    ifQuit.value = true
                },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically){
                Text("退出登录",modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp))
                Icon(Icons.Filled.ChevronRight, "前往", modifier = Modifier.padding(end = 16.dp))
                if (ifQuit.value) {
                    AlertDialog(
                        onDismissRequest = { ifQuit.value = false },
                        title = { Text("退出登录") },
                        text = { Text("确定要退出登录吗？") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    ifQuit.value = false
                                    loginViewModel.quitLogin(context)
                                    messageViewModel.reset()
                                    Toast.makeText(context, "退出成功", Toast.LENGTH_SHORT).show()
                                    navController.navigate("LoginScreen") {
                                        popUpTo("LoginScreen") { inclusive = true }
                                    }
                                }
                            ) {
                                Text("确认")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { ifQuit.value = false },
                            ) {
                                Text("取消")
                            }
                        }
                    )
                }
            }
        }
    }
}