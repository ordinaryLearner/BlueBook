package com.czcz.myapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(navController: NavController,viewmodel : ViewModel) {
    val context = LocalContext.current
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)
    val ifQuit = remember { mutableStateOf(false) }
    val myPosts = viewmodel.myPostList.collectAsState(initial = emptyList())
    val likedPosts = viewmodel.likedPostList.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val userInfo = DataStorePreference.getUser(context).collectAsState(initial = UserInfo(id = "", account = "", username = "BB用户", avatar = "https://picsum.photos/200/200", bio = "", joinTime = "", password = ""))

    val user = User(id = userInfo.value.id, account = userInfo.value.account, username = userInfo.value.username, avatar = userInfo.value.avatar?:"https://picsum.photos/200/200", password = userInfo.value.password, joinTime = userInfo.value.joinTime)


    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTab by remember { mutableIntStateOf(0) }


    LaunchedEffect(null) {
        viewmodel.getMyPosts(context)
    }
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        ProfileHeader(skyBlue, skyBlueDark, navController,user, viewmodel, context, ifQuit)

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = skyBlueDark,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "我的作品",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) skyBlueDark else Color.Gray
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "点赞作品",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) skyBlueDark else Color.Gray
                    )
                }
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val items = if (page == 0) myPosts else likedPosts
            PostGrid(items.value,navController,viewmodel)
        }
    }
}

@Composable
private fun ProfileHeader(skyBlue: Color, skyBlueDark: Color, navController: NavController,user :User, viewmodel: ViewModel, content: Context, ifQuit: MutableState<Boolean>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(skyBlue)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "个人主页",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { ifQuit.value = true }) {
                Icon(
                    imageVector = Icons.Filled.PowerSettingsNew,
                    contentDescription = "退出登录",
                    tint = Color.Red
                )
            }
        }

        if (ifQuit.value) {
            AlertDialog(
                onDismissRequest = { ifQuit.value = false },
                title = { Text("退出登录") },
                text = { Text("确定要退出登录吗？") },
                confirmButton = {
                    Button(
                        onClick = {
                            ifQuit.value = false
                            viewmodel.quitLogin(content)
                            Toast.makeText(content, "退出成功", Toast.LENGTH_SHORT).show()
                            navController.navigate("LoginScreen") {
                                popUpTo("LoginScreen") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { ifQuit.value = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatar,
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = user.username?:"BB用户",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "Lv.6",
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
            StatItem(count = "1.2w", label = "粉丝")
            StatItem(count = "86", label = "关注")
            StatItem(count = "3.6w", label = "获赞")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

}


@Composable
private fun StatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PostGrid(items: List<Post>, navController: NavController,viewModel: ViewModel) {
    if(items.isNotEmpty()){
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                Box(modifier = Modifier.clickable(onClick = {
                    navController.navigate("DetailScreen")
                    viewModel.setCurrentPost(item)
                })){
                    AsyncImage(
                        model = item.medias.firstOrNull()?.url ?: R.drawable.ic_image_placeholder,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(2.dp))
                    )
                    Column(){
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Filled.FavoriteBorder,
                                contentDescription = "点赞",
                                tint = Color(0xFFFFFFFF),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {

                                    }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.likes.size  >= 10000) {
                                    String.format("%.1fw", item.likes.size / 10000.0)
                                } else {
                                    item.likes.toString()
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFFFFFFF)
                            )
                        }
                    }
                }
            }
        }
    }
    else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text(
                text = "暂无数据",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
