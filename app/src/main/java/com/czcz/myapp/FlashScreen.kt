package com.czcz.myapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashScreen(navController: NavController, viewModel: ViewModel) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val feedItems by viewModel.postList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    LaunchedEffect(null) {
        viewModel.updatePost()
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(skyBlue)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索内容...", color = Color.White.copy(alpha = 0.7f)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "搜索",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "更多",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .padding(8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.updatePost() },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.padding(paddingValues)){
                if(feedItems.isEmpty()){
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                        Text(
                            text = "暂无数据",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else{
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(feedItems) { item ->
                            PostCard(item, navController, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(item: Post, navController: NavController, viewModel: ViewModel) {
    val skyBlueDark = Color(0xFF5BB0D9)
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(item.likes.size) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("DetailScreen")
                viewModel.setCurrentPost(item)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.medias.firstOrNull()?.url ?: R.drawable.ic_image_placeholder,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        if (item.id.hashCode() % 3 == 0) 0.75f
                        else if (item.id.hashCode() % 3 == 1) 1.0f
                        else 1.25f
                    )
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,){
                        AsyncImage(
                            model = "https://picsum.photos/100/100",
                            contentDescription = "头像",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.sender.username ?: item.sender.account,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    isLiked = !isLiked
                                    likeCount = if (isLiked) likeCount + 1 else likeCount
                                }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (likeCount >= 10000) {
                                String.format("%.1fw", likeCount / 10000.0)
                            } else {
                                likeCount.toString()
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }
            }
        }
    }
}
