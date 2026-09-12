package com.czcz.myapp.Screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.R

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostListScreen(title:String, postViewModel: PostViewModel, navController: NavController, loginViewModel: LoginViewModel) {
    val context = LocalContext.current
    val user by DataStorePreference.getUser(context).collectAsState(User.empty())
    val postType by postViewModel.postType.collectAsState()
    val myPostList by postViewModel.myPostList.collectAsState()
    val likedPostList by postViewModel.likedPostList.collectAsState()
    val favouritePostList by postViewModel.favouritePostList.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontSize = 16.sp) },
                navigationIcon = { Icon(Icons.Filled.ArrowBack, "返回", modifier = Modifier.clickable { navController.popBackStack() }) }
            )
        }
    ) { paddingValue ->
        val post = when (postType) {
            PostType.MINE -> myPostList
            PostType.LIKE -> likedPostList
            PostType.FAVOURITE -> favouritePostList
        }
        if (post.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxSize().padding(paddingValue)
            ) {
                items(post, key = { it.id }) { item ->
                    val isLiked = item.likes.any { it == user.id }
                    val ifFollowed =
                        loginViewModel.followersList.collectAsState().value.any({ it == item.sender.id })
                    val ifStarred = item.favourite.any { it == user.id }
                    Box(modifier = Modifier.clickable(onClick = {
                        navController.navigate("DetailScreen/${isLiked}/${ifFollowed}/${ifStarred}")
                        postViewModel.recordHistory(context, item)
                        postViewModel.setCurrentPost(item)
                    })) {
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
                                        String.format(
                                            locale = null,
                                            "%.1fw",
                                            item.likes.size / 10000.0
                                        )
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
}
