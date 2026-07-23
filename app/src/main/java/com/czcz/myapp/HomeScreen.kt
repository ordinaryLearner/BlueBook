package com.czcz.myapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.czcz.myapp.Models.*



@Composable
fun HomeScreen(navController: NavController) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)

    val navItems = remember {
        listOf(
            BottomNavItem("首页", Icons.Filled.Home),
            BottomNavItem("发现", Icons.Filled.Explore),
            BottomNavItem("发布", Icons.Filled.AddCircle),
            BottomNavItem("消息", Icons.Filled.Notifications),
            BottomNavItem("我的", Icons.Filled.Person)
        )
    }

    val pagerState = rememberPagerState(pageCount = { navItems.size })
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
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
                2 -> PublishPage()
                3 -> MessagePage()
                4 -> MineScreen(navController)
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
fun PublishPage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        //PublishPageContent()
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
