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
        listOf(
            FeedItem("1", "https://picsum.photos/400/600", "今日份的好心情", 128),
            FeedItem("2", "https://picsum.photos/400/500", "分享我的穿搭日常", 256),
            FeedItem("3", "https://picsum.photos/400/700", "周末探店打卡", 89),
            FeedItem("4", "https://picsum.photos/400/450", "日落时分的城市", 342),
            FeedItem("5", "https://picsum.photos/400/550", "居家好物推荐", 67),
            FeedItem("6", "https://picsum.photos/400/650", "美食制作教程", 512),
            FeedItem("7", "https://picsum.photos/400/480", "旅行日记·大理", 198),
            FeedItem("8", "https://picsum.photos/400/620", "读书笔记分享", 76),
            FeedItem("9", "https://picsum.photos/400/530", "健身打卡第30天", 421),
            FeedItem("10", "https://picsum.photos/400/580", "手账排版灵感", 153),
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "首页") },
                    label = { Text("首页") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = skyBlueDark,
                        selectedTextColor = skyBlueDark,
                        indicatorColor = skyBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Explore, contentDescription = "发现") },
                    label = { Text("发现") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = skyBlueDark,
                        selectedTextColor = skyBlueDark,
                        indicatorColor = skyBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "发布") },
                    label = { Text("发布") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = skyBlueDark,
                        selectedTextColor = skyBlueDark,
                        indicatorColor = skyBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "消息") },
                    label = { Text("消息") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = skyBlueDark,
                        selectedTextColor = skyBlueDark,
                        indicatorColor = skyBlue.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = skyBlueDark,
                        selectedTextColor = skyBlueDark,
                        indicatorColor = skyBlue.copy(alpha = 0.2f)
                    )
                )
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
                FeedItemCard(item)
            }
        }
    }
}

@Composable
fun FeedItemCard(item: FeedItem) {
    val skyBlueDark = Color(0xFF5BB0D9)
    var isLiked by remember { mutableStateOf(item.isLiked) }
    var likeCount by remember { mutableIntStateOf(item.likeCount) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
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
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF333333)
                )
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
