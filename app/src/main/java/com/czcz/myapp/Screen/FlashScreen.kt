package com.czcz.myapp.Screen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.R
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.ui.Avatar
import com.czcz.myapp.ui.theme.skyBlue
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashScreen(
    navController: NavController,
    postViewModel: PostViewModel,
    loginViewModel: LoginViewModel
) {
    val context = LocalContext.current
    val user = DataStorePreference.getUser(context).collectAsState(User.empty())
    val searchQuery by postViewModel.searchQuery.collectAsState()
    val feedItems by postViewModel.postList.collectAsState()
    val isRefreshing by postViewModel.isRefreshing.collectAsState()
    val isLoadMore by postViewModel.isLoadMore.collectAsState()
    val isNoMore by postViewModel.isNoMore.collectAsState()
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current
    val gridState by postViewModel.gridState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val isSearching by postViewModel.isSearching.collectAsState()


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
                    onValueChange = {
                        postViewModel.setSearchQuery(it)
                        if (it.isEmpty()) {
                            postViewModel.setIsSearching(false)
                        } else {
                            postViewModel.setIsSearching(true)
                        }
                    },
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

                AnimatedVisibility(
                    visible = isSearching,
                    enter = expandHorizontally(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeIn(
                        animationSpec = tween(250)
                    ),
                    exit = shrinkHorizontally(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutSlowInEasing
                        )
                    ) + fadeOut(
                        animationSpec = tween(150)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .wrapContentWidth()
                            .background(
                                Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp)
                            .clickable {
                                coroutineScope.launch { gridState.animateScrollToItem(0) }
                                postViewModel.search(context)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "搜索",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    gridState.scrollToItem(0)
                    postViewModel.updatePost(context)
                    Log.d("FlashScreen", "Refreshed")
                }
            },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    onClick = { focusManager.clearFocus(true) },
                    indication = null
                ),
        ) {
            Box(modifier = Modifier) {
                if (feedItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "暂无数据",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            state = gridState,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalItemSpacing = 8.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            items(feedItems) { item ->
                                PostCard(
                                    item,
                                    navController,
                                    postViewModel,
                                    user.value,
                                    loginViewModel,
                                    context
                                )
                            }
                            item(span = StaggeredGridItemSpan.FullLine) {
                                if (isLoadMore || isNoMore) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isLoadMore) {
                                            Text("加载中...")
                                            CircularProgressIndicator()
                                        }
                                        if (isNoMore) Text("没有更多了")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        LaunchedEffect(gridState) {
            snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { index ->
                    if (index != null && index >= feedItems.size - 3 && !isLoadMore && !isNoMore) {
                        postViewModel.loadMorePost(context)
                        Log.d("FlashScreen", "Loading more posts...")
                    }
                }
        }
    }
}


@Composable
fun PostCard(
    item: Post,
    navController: NavController,
    postViewModel: PostViewModel,
    user: User,
    loginViewModel: LoginViewModel,
    context: Context
) {
    val isLiked = item.likes.any { it == user.id }
    val ifFollowed = loginViewModel.followersList.collectAsState().value.any { it == item.sender.id }
    val ifStarred = item.favourite.any { it == user.id }
    val likeCount = item.likes.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("PostCard", "${item.sender.fans}")
                if(item.medias.firstOrNull()?.type == MediaType.IMAGE){ navController.navigate("DetailScreen/${isLiked}/${ifFollowed}/${ifStarred}") }
                else if(item.medias.firstOrNull()?.type == MediaType.VIDEO){ navController.navigate("VideoDetailScreen/${isLiked}/${ifFollowed}/${ifStarred}") }
                postViewModel.recordHistory(context, item)
                postViewModel.setCurrentPost(item)
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            if (item.medias.isNotEmpty()) {
                if (item.medias.first().type == MediaType.IMAGE) {
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
                } else {
                    val url = item.medias.firstOrNull()?.url
                    val videoEnabledLoader = remember {
                        ImageLoader.Builder(context)
                            .components {
                                add(VideoFrameDecoder.Factory())
                            }
                            .build()
                    }
                    if (url != null) {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .videoFrameMillis(1000)
                            .build()
                        AsyncImage(
                            model = request,
                            imageLoader = videoEnabledLoader,
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
                    }
                }
            }
            else {
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
                            //.aspectRatio(0.75f)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(
                            url = item.sender.avatar,
                            size = 22.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.sender.username ?: item.sender.account,
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            maxLines = 1,
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "点赞",
                            tint = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
                            modifier = Modifier
                                .size(18.dp)

                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (likeCount >= 10000) {
                                String.format(locale = null, "%.1fw", likeCount / 10000.0)
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
