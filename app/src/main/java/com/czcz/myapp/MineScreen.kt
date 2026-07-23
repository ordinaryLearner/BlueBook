package com.czcz.myapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MineScreen(navController: NavController) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)

    val dummyUser = User(
        username = "我",
        account = "me",
        password = "",
        joinTime = "2024-01-01",
        followers = 0,
        likes = 0
    )

    val myPosts = remember {
        listOf(
            Post("p1", Media(MediaType.IMAGE, "https://picsum.photos/400/600"), dummyUser, 128),
            Post("p2", Media(MediaType.IMAGE, "https://picsum.photos/400/500"), dummyUser, 256),
            Post("p3", Media(MediaType.IMAGE, "https://picsum.photos/400/700"), dummyUser, 89),
            Post("p4", Media(MediaType.IMAGE, "https://picsum.photos/400/450"), dummyUser, 342),
            Post("p5", Media(MediaType.IMAGE, "https://picsum.photos/400/550"), dummyUser, 67),
            Post("p6", Media(MediaType.IMAGE, "https://picsum.photos/400/650"), dummyUser, 512),
        )
    }

    val likedPosts = remember {
        listOf(
            Post("l1", Media(MediaType.IMAGE, "https://picsum.photos/400/500"), dummyUser, 980),
            Post("l2", Media(MediaType.IMAGE, "https://picsum.photos/400/600"), dummyUser, 1200),
            Post("l3", Media(MediaType.IMAGE, "https://picsum.photos/400/450"), dummyUser, 45),
            Post("l4", Media(MediaType.IMAGE, "https://picsum.photos/400/700"), dummyUser, 760),
        )
    }

    val pagerState = rememberPagerState(pageCount = { 2 })
    var selectedTab by remember { mutableIntStateOf(0) }

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
        ProfileHeader(skyBlue, skyBlueDark, navController)

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
            PostGrid(items)
        }
    }
}

@Composable
private fun ProfileHeader(skyBlue: Color, skyBlueDark: Color, navController: NavController) {
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "设置",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://picsum.photos/200/200",
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
                    text = "用户昵称",
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
private fun PostGrid(items: List<Post>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items, key = { it.id }) { item ->
            AsyncImage(
                model = item.media.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(2.dp))
            )
        }
    }
}
