package com.czcz.myapp.Screen

import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.Models.User
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.Api.UserViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.ui.Avatar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    navController: NavController,
    postViewModel: PostViewModel,
    userViewModel: UserViewModel,
    infoViewModel: InfoViewModel,
    loginViewModel: LoginViewModel,
    isLiked: Boolean,
    ifFollowed: Boolean,
    ifStarred: Boolean
) {
    var isLiked by remember { mutableStateOf(isLiked) }
    var ifFollowed by remember { mutableStateOf(ifFollowed) }
    var ifStared by remember { mutableStateOf(ifStarred) }
    val context = LocalContext.current
    val currentPost by postViewModel.currentPost.collectAsState()
    var likeCount by remember { mutableIntStateOf(currentPost.likes.size) }
    val currentComment by postViewModel.currentComment.collectAsState()
    val commentSize by postViewModel.commentSize.collectAsState()
    val errorMessage by postViewModel.errorMessage.collectAsState()
    val userErrorMessage by userViewModel.errorMessage.collectAsState()
    val ifComment by postViewModel.ifComment.collectAsState()
    val user by DataStorePreference.getUser(context).collectAsState(User.empty())
    val uri = currentPost.medias.first().url
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isCommentUser by postViewModel.isCommentUser.collectAsState()
    val inputContent by postViewModel.inputContent.collectAsState()
    val currentReceiver by postViewModel.currentReceiver.collectAsState()
    val isInputting by postViewModel.isInputting.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()

    val animateLikeColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFF4D6A) else Color(0xFFFFFFFF),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animateStarColor by animateColorAsState(
        targetValue = if (ifStared) Color(0xFFFAE423) else Color(0xFFFFFFFF),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animatedFollowColor by animateColorAsState(
        targetValue = if (ifFollowed) Color(0xFFFFFFFF) else Color(0xFFFF4D6A),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animatedFollowIconColor by animateColorAsState(
        targetValue = if (ifFollowed) Color(0xFFFF4D6A) else Color(0xFFFFFFFF),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )

    LaunchedEffect(currentPost, currentComment) {
        postViewModel.defaultCommentSize()
    }

    LaunchedEffect(errorMessage, userErrorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            postViewModel.resetErrorMessage()
        }
        if (userErrorMessage.isNotEmpty())
            Toast.makeText(context, userErrorMessage, Toast.LENGTH_SHORT).show()
        userViewModel.resetErrorMessage()
    }

    LaunchedEffect(ifComment) {
        if (ifComment) {
            Toast.makeText(context, "评论成功！", Toast.LENGTH_SHORT).show()
            postViewModel.setIfComment(false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            DisposableEffect(Unit) {
                onDispose { exoPlayer.release() }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                    }
                }
            )
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
                Text(
                    text = "评论 ($commentSize)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = interactionSource,
                            onClick = {
                                focusManager.clearFocus(true)
                                postViewModel.setIsCommentUser(false)
                            },
                            indication = null
                        )
                ) {
                    val comments = currentPost.comments ?: emptyList()
                    if (comments.isNotEmpty()) {
                        items(
                            count = comments.size,
                        ) { comment ->
                            CommentItem(
                                coroutineScope = rememberCoroutineScope(),
                                comment = comments[comment],
                                post = currentPost,
                                context = context,
                                navController = navController,
                                infoViewModel = infoViewModel,
                                postViewModel = postViewModel
                            )
                            Divider(
                                color = Color(0xFFF0F0F0),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无评论",
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999)
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .height(80.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(40.dp),
                        value = inputContent,
                        onValueChange = {
                            postViewModel.setInputting(true)
                            postViewModel.setInputContent(it)
                        },
                        label = {
                            when (isCommentUser) {
                                false -> Text("留下你的感想吧！")
                                true -> Text("回复 ${currentReceiver.username}")
                            }
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedVisibility(
                        visible = isInputting,
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
                                .width(80.dp)
                                .background(
                                    Color.Blue.copy(alpha = 0.15f),
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable(
                                    onClick = {
                                        postViewModel.comment(context)
                                    }, enabled = !isLoading
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "发送中",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "发送",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box() {
                Avatar(
                    url = currentPost.sender.avatar,
                    contentDescription = "Avatar",
                    size = 50.dp,
                    modifier = Modifier
                        .background(Color.Transparent)
                        .clip(CircleShape)
                        .padding(bottom = 5.dp)
                        .clickable {
                            infoViewModel.getDetailUser(context, currentPost.sender.id)
                            postViewModel.getUserPost(context, currentPost.sender.id)
                            navController.navigate("UserDetailScreen")
                        }
                )
                OutlinedButton(
                    contentPadding = PaddingValues(0.dp),
                    onClick = {
                        if (user.id != currentPost.sender.id) {
                            if (ifFollowed) loginViewModel.deleteFollower(currentPost.sender.id)
                            else loginViewModel.addFollower(currentPost.sender.id)
                            ifFollowed = !ifFollowed
                            userViewModel.followUser(
                                context,
                                currentPost.sender.id,
                                ifFollowed
                            )
                            Log.d("DetailScreen", "ifFollowed: $ifFollowed")
                        } else {
                            Toast.makeText(context, "不能关注自己！", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = animatedFollowColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .align(Alignment.BottomCenter)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "关注",
                        tint = animatedFollowIconColor,
                    )
                }
            }
            Spacer(modifier = Modifier.height(15.dp))
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "点赞",
                tint = animateLikeColor,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        postViewModel.like(context, isLiked)
                    }
            )
            Text(
                text = if (likeCount >= 10000) {
                    String.format(Locale.getDefault(), "%.1fw", likeCount / 10000.0)
                } else {
                    likeCount.toString()
                },
                fontSize = 16.sp,
                color = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Icon(
                imageVector = Icons.Filled.ModeComment,
                contentDescription = "评论",
                tint = Color.White,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        showBottomSheet = true
                    }
            )
            Text(
                text = if (commentSize >= 10000) {
                    String.format(Locale.getDefault(), "%.1fw", commentSize / 10000.0)
                } else {
                    commentSize.toString()
                },
                fontSize = 16.sp,
                color = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "收藏",
                tint = animateStarColor,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        ifStared = !ifStared
                        postViewModel.star(context, ifStared)
                    }
            )
        }

        Row(modifier = Modifier.padding(16.dp)){
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.clickable{
                    navController.popBackStack()
                }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column() {
                Text(
                    currentPost.sender.username ?: "BB用户",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(currentPost.title, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(currentPost.content, fontSize = 16.sp, color = Color.White)
            }
            ExoPlayerSlider(exoPlayer)
        }
    }
}

@Composable
fun ExoPlayerSlider(player: ExoPlayer) {
    var currentPosition by remember { mutableStateOf(0L) }
    var isDragging by remember { mutableStateOf(false) }
    var dragPosition by remember { mutableStateOf(0f) }
    val duration = remember { mutableStateOf(0L) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                duration.value = player.duration
            }
        }
        player.addListener(listener)

        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player) {
        while (true) {
            if (player.playWhenReady && !isDragging) {
                currentPosition = player.currentPosition
            }
            delay(16)
        }
    }

    val safeDuration = duration.value.coerceAtLeast(1L)
    val sliderValue = if (isDragging) {
        dragPosition
    } else {
        currentPosition.toFloat() / safeDuration.toFloat()
    }

    // 5. Slider 组件
    MinimalVideoProgressBar(
        progress = sliderValue,
        onSeekStart = {
            isDragging = true
        },
        onSeek = { newValue ->
            dragPosition = newValue
        },
        onSeekEnd = {
            player.seekTo((dragPosition * safeDuration).toLong())
            isDragging = false
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MinimalVideoProgressBar(
    progress: Float,
    onSeekStart: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    var widthPx by remember { mutableStateOf(1f) }
    var isDragging by remember { mutableStateOf(false) }
    val height by animateDpAsState(if (isDragging) 4.dp else 2.dp)
    val size by animateDpAsState(if (isDragging) 14.dp else 8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(Unit) {//点击跳转
                detectTapGestures(
                    onTap = { offset ->
                        onSeek((offset.x / widthPx).coerceIn(0f, 1f))
                    }
                )
            }
            .pointerInput(Unit) {//拖动跳转
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                        onSeekStart()
                    },
                    onDragEnd = {
                        isDragging = false
                        onSeekEnd()
                    },
                    onDragCancel = {
                        isDragging = false
                        onSeekEnd()
                    }
                ) { change, _ ->
                    onSeek((change.position.x / widthPx).coerceIn(0f, 1f))
                    change.consume()
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(1.dp))
        )

        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(height)
                .background(Color.White, RoundedCornerShape(1.dp))
        )

        Box(
            Modifier
                .offset { IntOffset((widthPx * progress).toInt() - 1, 0) }
                .size(size)
                .background(Color.White, CircleShape)
        )
    }
}
