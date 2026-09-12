package com.czcz.myapp.Screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.R
import com.czcz.myapp.ui.theme.skyBlueDark
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.ui.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(navController: NavController, loginViewModel: LoginViewModel,postViewModel: PostViewModel,infoViewModel: InfoViewModel, messageViewModel: MessageViewModel) {
    val context = LocalContext.current
    val user by infoViewModel.detailUser.collectAsState()
    val currentUser = DataStorePreference.getUser(context).collectAsState(User.empty())
    val userPost = postViewModel.detailUserPost.collectAsState().value
    val animatedLevel = remember { Animatable(0f) }
    val selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(user) {
        val currentTime = System.currentTimeMillis()
        val level = (currentTime - user.toTimestamp()) / (1000L * 60 * 60 * 24 )
        animatedLevel.animateTo(
            level.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${user.username}的主页") },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBack,
                        "返回",
                        modifier = Modifier.clickable { navController.popBackStack() })
                }
            )
        },
    ) {
        Column(modifier = Modifier.padding(it)){
            Box(modifier = Modifier
                .fillMaxWidth()
                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable {
                            navController.navigate("ImageViewScreen")
                            postViewModel.checkImage(
                                listOf(user.background?.toUri() ?: Uri.EMPTY),
                                0
                            )
                        }
                ) {
                    Log.d("MineScreen", "background: ${user.background}")
                    AsyncImage(
                        contentScale = ContentScale.Crop,
                        model = if(user.background != null && user.background != "") user.background else R.drawable.default_background,
                        contentDescription = "background"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                }
                Column() {
                    UserProfileHeader(currentUser.value,user, loginViewModel, navController, postViewModel, messageViewModel,animatedLevel)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = skyBlueDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { },
                            text = {
                                Text(
                                    "我的作品",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) skyBlueDark else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
            if(userPost.isNotEmpty()){
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                ) {
                    items(userPost) { post ->
                        PostItem(
                            post,
                            navController,
                            postViewModel,
                            loginViewModel,
                            currentUser.value,
                            context,
                        )
                    }
                }
            }
            else{
                Box(modifier = Modifier.fillMaxSize()){
                    Text(
                        text = "该用户暂无作品",
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    currentUser:User,
    user: User,
    loginViewModel: LoginViewModel,
    navController: NavController,
    postViewModel: PostViewModel,
    messageViewModel: MessageViewModel,
    animatedLevel: Animatable<Float, AnimationVector1D>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .statusBarsPadding()
    ) {

//

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                url = user.avatar,
                size = 80.dp,
                modifier = Modifier.clickable{
                    navController.navigate("ImageViewScreen")
                    postViewModel.checkImage(listOf(user.background?.toUri() ?: Uri.EMPTY), 0)
                }
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username ?: "BB用户",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (currentUser.id != user.id){
                        Button(
                            onClick = {
                                messageViewModel.setCurrentPartner(user)
                                navController.navigate("ChatScreen")
                            },
                            contentPadding = PaddingValues(2.dp),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, Color.White),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(alpha = 0.3f),
                                contentColor = Color.White
                            )
                        ) {
                            Text("私信")
                        }
                    }
                }
                Text(
                    text = user.bio ?: "这家伙很懒，什么都没留下",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "level ${animatedLevel.value.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Log.d("MineScreen", "StatItem: ${loginViewModel.fansList.collectAsState().value}")
            Log.d("MineScreen", "StatItem: ${loginViewModel.followersList.collectAsState().value}")
            StatItem(count = user.fans?.size.toString(), label = "粉丝")
            StatItem(count = user.followers?.size.toString(), label = "关注")
            StatItem(count = user.likes.toString(), label = "获赞")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

}
