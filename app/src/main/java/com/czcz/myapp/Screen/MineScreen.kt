package com.czcz.myapp.Screen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.R
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.ui.Avatar
import com.czcz.myapp.ui.theme.skyBlueDark
import androidx.compose.runtime.collectAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(navController: NavController, postViewModel: PostViewModel, loginViewModel: LoginViewModel, infoViewModel: InfoViewModel) {
    val context = LocalContext.current
    val myPosts = postViewModel.myPostList.collectAsState(initial = emptyList())
    val likedPosts = postViewModel.likedPostList.collectAsState(initial = emptyList())
    val user by DataStorePreference.getUser(context).collectAsState(initial = User.empty())
    val errorMessage by postViewModel.errorMessage.collectAsState()
    val infoErrorMessage by infoViewModel.errorMessage.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTab by remember { mutableIntStateOf(0) }
    val animatedLevel = remember { Animatable(0f) }



    LaunchedEffect(null) {
        infoViewModel.getMe(context)
        postViewModel.getMyPosts(context)
        postViewModel.getLikedPost(context)
        postViewModel.getFavouritePost(context)
        postViewModel.getHistory(context)
        val currentTime = System.currentTimeMillis()
        val level = (currentTime - user.toTimestamp()) / (1000L * 60 * 60 * 24 * 1000  )
        Log.d("MineScreen", "level $level")
        Log.d("MineScreen", "level $currentTime")
        Log.d("MineScreen", "level ${user.toTimestamp()}")
        Log.d("MineScreen", "level ${user.joinTime}")
        animatedLevel.animateTo(
            level.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            Log.d("MineScreen", "Error: $errorMessage")
            postViewModel.resetErrorMessage()
        }
    }
    LaunchedEffect(infoErrorMessage) {
        if (infoErrorMessage.isNotEmpty()) {
            Toast.makeText(context, infoErrorMessage, Toast.LENGTH_SHORT).show()
            infoViewModel.resetErrorMessage()
        }
    }

    Column(
        modifier = Modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()){
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clickable {
                        navController.navigate("BackgroundScreen")
                    }
                ){
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
                    ProfileHeader(navController, user, loginViewModel,context, infoViewModel, animatedLevel)
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
                }
            }


            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) { page ->
                val items = if (page == 0) myPosts else likedPosts
                PostGrid(items.value, navController, postViewModel, loginViewModel,user, )

            }
        }
    }
}

@Composable
fun ProfileHeader(
    navController: NavController,
    user: User,
    loginViewModel: LoginViewModel,
    context: Context,
    infoViewModel: InfoViewModel,
    animatedLevel: Animatable<Float, AnimationVector1D>
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {navController.navigate("SettingScreen")}) {
                Icon(
                    imageVector = Icons.Filled.MoreHoriz,
                    contentDescription = "更多功能",
                    tint = Color.White
                )
            }
        }

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
                modifier = Modifier.clickable {
                    navController.navigate("AvatarScreen")
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
                    IconButton(onClick = {
                        infoViewModel.setNewUsername(user.username ?: "")
                        infoViewModel.setNewBio(user.bio ?: "")
                        infoViewModel.setIsUpdateProfile(true)
                        navController.navigate("UpdateProfileScreen")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "编辑",
                            tint = Color.White
                        )
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
            StatItem(count = loginViewModel.fansList.collectAsState().value.size.toString(), label = "粉丝")
            StatItem(count = loginViewModel.followersList.collectAsState().value.size.toString(), label = "关注")
            StatItem(count = user.likes.toString(), label = "获赞")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

}


@Composable
fun StatItem(count: String, label: String) {
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
private fun PostGrid(
    items: List<Post>,
    navController: NavController,
    postViewModel: PostViewModel,
    loginViewModel: LoginViewModel,
    user: User
) {
    val context = LocalContext.current
    if (items.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                PostItem(item, navController, postViewModel, loginViewModel, user, context)
            }
            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("暂无更多")
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "暂无数据",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
@Composable
fun PostItem(item: Post, navController: NavController, postViewModel: PostViewModel, loginViewModel: LoginViewModel, user: User, context: Context){
    val isLiked = item.likes.any { it == user.id }
    val ifFollowed = loginViewModel.followersList.collectAsState().value.any({ it == item.sender.id })
    val ifStarred = item.favourite.any { it == user.id }
    Box(modifier = Modifier.clickable(onClick = {
        if(item.medias.firstOrNull()?.type == MediaType.IMAGE){ navController.navigate("DetailScreen/${isLiked}/${ifFollowed}/${ifStarred}") }
        else if(item.medias.firstOrNull()?.type == MediaType.VIDEO){ navController.navigate("VideoDetailScreen/${isLiked}/${ifFollowed}/${ifStarred}") }
        postViewModel.recordHistory(context, item)
        postViewModel.setCurrentPost(item)
    })) {
        if(item.medias.firstOrNull()?.type == MediaType.IMAGE){
            AsyncImage(
                model = item.medias.firstOrNull()?.url
                    ?: R.drawable.ic_image_placeholder,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
        else{
            val videoEnabledLoader = remember {
                ImageLoader.Builder(context)
                    .components {
                        add(VideoFrameDecoder.Factory())
                    }
                    .build()
            }
            val url = item.medias.firstOrNull()?.url
            val request = if(url != null){
                ImageRequest.Builder(context)
                    .data(url)
                    .videoFrameMillis(1000)
                    .build()
            }else R.drawable.ic_image_placeholder
            AsyncImage(
                model = request,
                imageLoader = videoEnabledLoader,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.shadow(elevation = 4.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
                    modifier = Modifier
                        .size(18.dp)

                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (item.likes.size >= 10000) {
                        String.format(locale = null, "%.1fw", item.likes.size / 10000.0)
                    } else {
                        item.likes.size.toString()
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFFFFFFF)
                )
            }
        }
    }
}
