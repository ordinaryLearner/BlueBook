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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashScreen(navController: NavController) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val feedItems = remember {
        val mockUser = User(username = "小明", account = "xiaoming", password = "", joinTime = "2024-01-01", followers = 1200, likes = 3600)
        listOf(
            Post("1", Media(MediaType.IMAGE, "https://picsum.photos/400/600"), mockUser, 128),
            Post("2", Media(MediaType.IMAGE, "https://picsum.photos/400/500"), mockUser, 256),
            Post("3", Media(MediaType.IMAGE, "https://picsum.photos/400/700"), mockUser, 89),
            Post("4", Media(MediaType.IMAGE, "https://picsum.photos/400/450"), mockUser, 342),
            Post("5", Media(MediaType.IMAGE, "https://picsum.photos/400/550"), mockUser, 67),
            Post("6", Media(MediaType.IMAGE, "https://picsum.photos/400/650"), mockUser, 512),
            Post("7", Media(MediaType.IMAGE, "https://picsum.photos/400/480"), mockUser, 198),
            Post("8", Media(MediaType.IMAGE, "https://picsum.photos/400/620"), mockUser, 76),
            Post("9", Media(MediaType.IMAGE, "https://picsum.photos/400/530"), mockUser, 421),
            Post("10", Media(MediaType.IMAGE, "https://picsum.photos/400/580"), mockUser, 153),
        )
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
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 8.dp,
                start = 8.dp,
                end = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            items(feedItems, key = { it.id }) { item ->
                PostCard(item, navController)
            }
        }
    }
}

@Composable
fun PostCard(item: Post, navController: NavController) {
    val skyBlueDark = Color(0xFF5BB0D9)
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(item.likes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("PostDetailScreen/${item.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.media.url,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                isLiked = !isLiked
                                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
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
