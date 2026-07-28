package com.czcz.myapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController



@Composable
fun HomeScreen(navController: NavController) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)

    val navItemsLeft = remember {
        listOf(
            BottomNavItem("首页", Icons.Filled.Home),
            BottomNavItem("发现", Icons.Filled.Explore)
        )
    }
    val navItemsRight = remember {
        listOf(
            BottomNavItem("消息", Icons.Filled.Notifications),
            BottomNavItem("我的", Icons.Filled.Person)
        )
    }

    //val totalPages = 4
    val pagerState = rememberPagerState(pageCount = {4})
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.settledPage) {
        selectedTab = pagerState.settledPage
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItemsLeft.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = skyBlueDark,
                                selectedTextColor = skyBlueDark,
                                indicatorColor = skyBlue.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(skyBlue)
                            .clickable { navController.navigate("PublishScreen") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "发布",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    navItemsRight.forEachIndexed { index, item ->
                        val pageIndex = index + 2
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedTab == pageIndex,
                            onClick = { selectedTab = pageIndex },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = skyBlueDark,
                                selectedTextColor = skyBlueDark,
                                indicatorColor = skyBlue.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) { page ->
            when (page) {
                0 -> FlashPage(navController)
                1 -> DiscoverPage()
                2 -> MessagePage()
                3 -> MineScreen(navController)
            }
        }
    }
}

@Composable
fun FlashPage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        FlashScreen(navController = navController)
    }
}

@Composable
fun DiscoverPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        //DiscoverPageContent()
    }
}



@Composable
fun MessagePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        //MessagePageContent()
    }
}
