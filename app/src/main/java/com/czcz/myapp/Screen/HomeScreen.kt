package com.czcz.myapp.Screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.NavigationViewModel
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.Api.UserViewModel
import com.czcz.myapp.ui.theme.skyBlue
import com.czcz.myapp.ui.theme.skyBlueDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    postViewModel: PostViewModel,
    loginViewModel: LoginViewModel,
    infoViewModel: InfoViewModel,
    userViewModel: UserViewModel,
    messageViewModel: MessageViewModel
) {
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
    val context = LocalContext.current
    val navigationViewModel: NavigationViewModel = viewModel()
    val pagerState = rememberPagerState(pageCount = {4})
    val selectedTab by navigationViewModel.selectedIndex.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val gridState by postViewModel.gridState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            postViewModel.setVideoUri(uri)
            postViewModel.uriToCacheFile(context)
            navController.navigate("VideoViewScreen")
        }
    }


    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
        Log.d("HomeScreen", "animateScrollToPage $selectedTab")
    }
    LaunchedEffect(pagerState.settledPage) {
        navigationViewModel.setSelectedIndex(pagerState.settledPage)
        Log.d("HomeScreen", "setSelectedIndex ${pagerState.settledPage}")
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selectedTab == 1) Color.Black else Color.White),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItemsLeft.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = selectedTab == index,
                            onClick = {
                                Log.d("HomeScreen", "Clicked on $selectedTab")
                                if(selectedTab == 0 && index == 0){
                                    coroutineScope.launch{
                                        gridState.animateScrollToItem(0)
                                        postViewModel.updatePost(context)
                                        Log.d("HomeScreen", "selected updatePost")
                                    }
                                }
                                navigationViewModel.setSelectedIndex(index)
                                      },
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
                            .clickable { showBottomSheet = true },
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
                            onClick = { navigationViewModel.setSelectedIndex(pageIndex) },
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
                0 -> FlashScreen(navController, postViewModel, loginViewModel)
                1 -> FindScreen(navController, postViewModel, userViewModel, infoViewModel,loginViewModel)
                2 -> ChatListScreen(navController, messageViewModel)
                3 -> MineScreen(navController, postViewModel,loginViewModel, infoViewModel)
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showBottomSheet = false
                    }
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "选择内容类型",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                                pickVideoLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                                postViewModel.setPublishType(PublishType.VIDEOPOST)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("选择视频")
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    if (!sheetState.isVisible) showBottomSheet = false
                                }
                                navController.navigate("ImagePublishScreen")
                                postViewModel.setPublishType(PublishType.IMAGEPOST)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("选择图片")
                    }
                }
            }
        }
    }
}







@Composable
fun MessagePage() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Text("Message Page")
    }
}
