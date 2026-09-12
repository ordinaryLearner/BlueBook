package com.czcz.myapp.Screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.R
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.DataStorePreference


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryPostScreen(
    postViewModel: PostViewModel,
    navController: NavController,
    loginViewModel: LoginViewModel,
) {
    val context = LocalContext.current
    val dayHistory = postViewModel.dayHistory.collectAsState().value
    val yesterdayHistory = postViewModel.yesterdayHistory.collectAsState().value
    val weekHistory = postViewModel.weekHistory.collectAsState().value
    val monthHistory = postViewModel.monthHistory.collectAsState().value
    val user = DataStorePreference.getUser(context).collectAsState(User.empty())


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("历史记录") },
                navigationIcon = {
                    Icon(
                        Icons.Filled.ArrowBack,
                        "返回",
                        modifier = Modifier.clickable { navController.popBackStack() })
                }
            )
        },
    ) { paddingValue ->
        if (dayHistory.isEmpty() && yesterdayHistory.isEmpty() && weekHistory.isEmpty() && monthHistory.isEmpty())
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "暂无数据",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            }
        // 1. 今天的帖子
        else {
            Log.d("HistoryPostScreen", "dayHistory: $dayHistory")
            Log.d("HistoryPostScreen", "yesterdayHistory: $yesterdayHistory")
            Log.d("HistoryPostScreen", "weekHistory: $weekHistory")
            Log.d("HistoryPostScreen", "monthHistory: $monthHistory")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue)
            ) {
                if (dayHistory.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Row(modifier = Modifier.padding(start = 10.dp)){ Text("今天", fontSize = 40.sp) }
                    }
                    items(items = dayHistory, key = { it.id }) { post ->
                        PostItem(
                            item = post,
                            user = user.value,
                            loginViewModel = loginViewModel,
                            navController = navController,
                            postViewModel = postViewModel,
                            context = context
                        )
                    }
                }
                // 2. 昨天的帖子
                if (yesterdayHistory.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Row(modifier = Modifier.padding(start = 10.dp)){ Text("昨天", fontSize = 40.sp) }
                    }
                    items(items = yesterdayHistory, key = { it.id }) { post ->
                        PostItem(
                            item = post,
                            user = user.value,
                            loginViewModel = loginViewModel,
                            navController = navController,
                            postViewModel = postViewModel,
                            context = context
                        )
                    }
                }

                // 3. 一周前的帖子
                if (weekHistory.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Row(modifier = Modifier.padding(start = 10.dp)){ Text("一周前", fontSize = 40.sp) }
                    }
                    items(items = weekHistory, key = { it.id }) { post ->
                        PostItem(
                            item = post,
                            user = user.value,
                            loginViewModel = loginViewModel,
                            navController = navController,
                            postViewModel = postViewModel,
                            context = context
                        )
                    }
                }
                // 4. 一月前的帖子
                if (monthHistory.isNotEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Row(modifier = Modifier.padding(start = 10.dp)){ Text("以前", fontSize = 40.sp) }
                    }
                    items(items = monthHistory, key = { it.id }) { post ->
                        PostItem(
                            item = post,
                            user = user.value,
                            loginViewModel = loginViewModel,
                            navController = navController,
                            postViewModel = postViewModel,
                            context = context
                        )
                    }
                }
                item(span = { GridItemSpan(3) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(50.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text("暂无更多")
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

//@Composable
//fun PostItem(
//    item: Post,
//    user: User,
//    loginViewModel: LoginViewModel,
//    navController: NavController,
//    postViewModel: PostViewModel
//) {
//    val context = LocalContext.current
//    val isLiked = item.likes.any { it == user.id }
//    val ifFollowed =
//        loginViewModel.followersList.collectAsState().value.any({ it == item.sender.id })
//    val ifStarred = item.favourite.any { it == user.id }
//    Box(modifier = Modifier.clickable(onClick = {
//        navController.navigate("DetailScreen/${isLiked}/${ifFollowed}/${ifStarred}")
//        postViewModel.recordHistory(context, item)
//        postViewModel.setCurrentPost(item)
//    })) {
//        AsyncImage(
//            model = item.medias.firstOrNull()?.url
//                ?: R.drawable.ic_image_placeholder,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .aspectRatio(0.75f)
//                .clip(RoundedCornerShape(2.dp))
//        )
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomStart)
//                .padding(2.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.End,
//                modifier = Modifier.shadow(elevation = 4.dp)
//            ) {
//                Icon(
//                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
//                    contentDescription = "点赞",
//                    tint = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
//                    modifier = Modifier
//                        .size(18.dp)
//
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Text(
//                    text = if (item.likes.size >= 10000) {
//                        String.format(
//                            locale = null,
//                            "%.1fw",
//                            item.likes.size / 10000.0
//                        )
//                    } else {
//                        item.likes.size.toString()
//                    },
//                    fontSize = 12.sp,
//                    color = Color(0xFFFFFFFF)
//                )
//            }
//        }
//    }
//}