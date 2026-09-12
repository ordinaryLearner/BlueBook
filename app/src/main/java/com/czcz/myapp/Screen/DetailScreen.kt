package com.czcz.myapp.Screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.UserViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.ui.Avatar
import com.czcz.myapp.ui.theme.skyBlueDark
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.net.toUri
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DetailScreen(
    navController: NavController,
    postViewModel: PostViewModel,
    isLiked: Boolean,
    ifStarred: Boolean,
    userViewModel: UserViewModel,
    ifFollowed: Boolean,
    loginViewModel: LoginViewModel,
    infoViewModel: InfoViewModel
) {
    var isLiked by remember { mutableStateOf(isLiked) }
    var ifFollowed by remember { mutableStateOf(ifFollowed) }
    var ifStared by remember { mutableStateOf(ifStarred) }
    val post by postViewModel.currentPost.collectAsState()
    val pagerState = rememberPagerState(pageCount = { post.medias.size })
    val context = LocalContext.current
    val inputContent = postViewModel.inputContent.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val isInputing by postViewModel.isInputting.collectAsState()
    var likeCount by remember { mutableIntStateOf(post.likes.size) }
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isCommentUser by postViewModel.isCommentUser.collectAsState()
    val ifComment by postViewModel.ifComment.collectAsState()
    val errorMessage by postViewModel.errorMessage.collectAsState()
    val userErrorMessage by userViewModel.errorMessage.collectAsState()
    val currentComment by postViewModel.currentComment.collectAsState()
    val currentReceiver by postViewModel.currentReceiver.collectAsState(User.empty())
    val commentSize by postViewModel.commentSize.collectAsState()
    val user by DataStorePreference.getUser(context).collectAsState(User.empty())
    val animatedFollowColor by animateColorAsState(
        targetValue = if (ifFollowed) Color(0xFF999999) else Color(0xFFFF4D6A),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animateLikeColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFF4D6A) else Color(0xFF999999),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animateStarColor by animateColorAsState(
        targetValue = if (ifStared) Color(0xFFFAE423) else Color(0xFF999999),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animatedWidth by animateDpAsState(
        targetValue = if (ifFollowed) 70.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )

    LaunchedEffect(post, currentComment) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        onClick = {
                            focusManager.clearFocus(true)
                            postViewModel.setIsCommentUser(false)
                        },
                        indication = null
                    ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(
                                url = post.sender.avatar,
                                size = 32.dp,
                                modifier = Modifier.clickable {
                                    infoViewModel.getDetailUser(context, post.sender.id)
                                    postViewModel.getUserPost(context, post.sender.id)
                                    navController.navigate("UserDetailScreen")
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = post.sender.username ?: "BB用户",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                modifier = Modifier.clickable {
                                    infoViewModel.getDetailUser(context, post.sender.id)
                                    postViewModel.getUserPost(context, post.sender.id)
                                    navController.navigate("UserDetailScreen")
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                contentPadding = PaddingValues(0.dp),
                                onClick = {
                                    if (user.id != post.sender.id) {
                                        if (ifFollowed) loginViewModel.deleteFollower(post.sender.id)
                                        else loginViewModel.addFollower(post.sender.id)
                                        ifFollowed = !ifFollowed
                                        userViewModel.followUser(
                                            context,
                                            post.sender.id,
                                            ifFollowed
                                        )
                                        Log.d("DetailScreen", "ifFollowed: $ifFollowed")
                                    } else {
                                        Toast.makeText(context, "不能关注自己！", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }, modifier = Modifier
                                    .clip(CircleShape)
                                    .height(30.dp)
                                    .width(animatedWidth),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = animatedFollowColor,
                                    contentColor = Color.White
                                )
                            ) {
                                AnimatedContent(targetState = ifFollowed) {
                                    if (it) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("已关注", color = Color(0xFF797878))
                                        }
                                    } else {
                                        Icon(
                                            Icons.Filled.Add,
                                            contentDescription = "Add",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (ifStared) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star",
                                tint = animateStarColor,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable {
                                        ifStared = !ifStared
                                        postViewModel.star(context, ifStared)
                                    }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "点赞",
                                tint = animateLikeColor,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        isLiked = !isLiked
                                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                                        postViewModel.like(context, isLiked)
                                    }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (likeCount >= 10000) {
                                    String.format("%.1fw", likeCount / 10000.0)
                                } else {
                                    likeCount.toString()
                                },
                                fontSize = 16.sp,
                                color = Color(0xFF999999)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                        postViewModel.setIsCommentUser(false)
                        postViewModel.setInputContent("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
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
                    value = inputContent.value,
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
                    visible = isInputing,
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable(
                    interactionSource = interactionSource,
                    onClick = {
                        focusManager.clearFocus(true)
                        postViewModel.setIsCommentUser(false)
                    },
                    indication = null
                )
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color(0xFF1A1A1A))
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = post.medias[page].url,
                            contentDescription = "图片",
                            modifier = Modifier.fillMaxSize().clickable {
                                navController.navigate("ImageViewScreen")
                                postViewModel.checkImage(post.medias.map { it.url.toUri() }, page)
                            },
                            contentScale = ContentScale.Fit
                        )
                    }
                    if (post.medias.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${post.medias.size}",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        color = Color(0xCC000000),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = post.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = post.content,
                        fontSize = 15.sp,
                        color = Color(0xFF444444),
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "发布时间：${post.time}",
                        fontSize = 12.sp,
                        color = Color(0xFF999999)
                    )
                }
            }

            item {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color(0xFF6C6C6C)
                )
                Text(
                    text = "💬 全部评论 ($commentSize)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            val comments = post.comments ?: emptyList()
            if (comments.isNotEmpty()) {
                items(
                    count = comments.size,
                ) { comment ->
                    CommentItem(
                        coroutineScope = rememberCoroutineScope(),
                        comment = comments[comment],
                        post = post,
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
    }
}

@Composable
fun CommentItem(
    coroutineScope: CoroutineScope,
    comment: Comment,
    post: Post,
    context: Context,
    navController: NavController,
    infoViewModel: InfoViewModel,
    postViewModel: PostViewModel
) {
    val showTranslate = remember { mutableStateOf(false) }
    LaunchedEffect(key1 = comment.content) {
        showTranslate.value = postViewModel.detectLanguage(comment.content)
    }
    val ifShow = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(6.dp)),
        verticalAlignment = Alignment.Top,
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Avatar(
                url = comment.sender.avatar,
                size = 36.dp,
                modifier = Modifier.clickable {
                    infoViewModel.getDetailUser(context, post.sender.id)
                    postViewModel.getUserPost(context, post.sender.id)
                    navController.navigate("UserDetailScreen")
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.sender.username ?: "BB用户",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        modifier = Modifier.clickable {
                            infoViewModel.getDetailUser(context, post.sender.id)
                            postViewModel.getUserPost(context, post.sender.id)
                            navController.navigate("UserDetailScreen")
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (comment.sender.username == post.sender.username) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(24.dp)
                                .padding(start = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = Color(0xFF2196F3),
                                ),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFF2196F3)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "帖主",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                val textContent = remember { mutableStateOf(comment.content) }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = textContent.value,
                    fontSize = 11.sp,
                    color = Color(0xFF444444),
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row() {
                    Text(
                        text = comment.time,
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(modifier = Modifier.clickable(onClick = {
                        postViewModel.setIsCommentUser(true)
                        postViewModel.setCurrentComment(comment)
                        postViewModel.setCurrentReceiver(comment.sender)
                    }), text = "回复", fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(20.dp))
                    if(showTranslate.value){
                        if(!ifShow.value){
                            Text(modifier = Modifier.clickable(onClick = {
                                ifShow.value = !ifShow.value
                                coroutineScope.launch {
                                    textContent.value = postViewModel.translateText(comment.content)
                                }
                            }), text = "翻译", fontSize = 10.sp)
                        }
                        else{
                            Text(modifier = Modifier.clickable(onClick = {
                                ifShow.value = !ifShow.value
                                textContent.value = comment.content
                            }), text = "x", fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (comment.comments != null && comment.comments.isNotEmpty()) {
                    val index = remember { mutableIntStateOf(1) }
                    val replyList = remember { mutableStateListOf(comment.comments[0]) }

                    Column {
                        replyList.forEach {
                            val showTranslate = remember { mutableStateOf(false) }
                            val ifShow = remember { mutableStateOf(false) }
                            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                LaunchedEffect(key1 = it.content) {
                                    showTranslate.value = postViewModel.detectLanguage(it.content)
                                }
                                Avatar(
                                    url = it.sender.avatar,
                                    size = 36.dp,
                                    modifier = Modifier.clickable {
                                        infoViewModel.getDetailUser(context, it.sender.id)
                                        postViewModel.getUserPost(context, it.sender.id)
                                        navController.navigate("UserDetailScreen")
                                    }
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column() {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (it.sender.username == post.sender.username) it.sender.username
                                                ?: "" else "${it.sender.username} 回复 ${comment.sender.username}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF333333),
                                            modifier = Modifier.clickable {
                                                infoViewModel.getDetailUser(context, it.sender.id)
                                                postViewModel.getUserPost(context, it.sender.id)
                                                navController.navigate("UserDetailScreen")
                                            }
                                        )
                                        if (comment.sender.username == post.sender.username) {
                                            Box(
                                                modifier = Modifier
                                                    .height(20.dp)
                                                    .width(24.dp)
                                                    .padding(start = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Button(
                                                    onClick = {},
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.White,
                                                        contentColor = Color(0xFF2196F3),
                                                    ),
                                                    shape = RoundedCornerShape(4.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        width = 1.dp,
                                                        color = Color(0xFF2196F3)
                                                    ),
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text(
                                                        text = "帖主",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    val replyContent = remember { mutableStateOf(it.content) }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = replyContent.value,
                                        fontSize = 11.sp,
                                        color = Color(0xFF444444),
                                        lineHeight = 22.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row() {
                                        Text(
                                            text = it.time,
                                            fontSize = 10.sp,
                                            color = Color(0xFF999999)
                                        )
                                        Spacer(modifier = Modifier.width(20.dp))
                                        Text(modifier = Modifier.clickable(onClick = {
                                            postViewModel.setIsCommentUser(true)
                                            postViewModel.setCurrentComment(comment)
                                            postViewModel.setCurrentReceiver(it.sender)
                                        }), text = "回复", fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(20.dp))
                                        if(showTranslate.value){
                                            if(!ifShow.value){
                                                Text(modifier = Modifier.clickable(onClick = {
                                                    ifShow.value = !ifShow.value
                                                    coroutineScope.launch {
                                                        replyContent.value =
                                                            postViewModel.translateText(it.content)
                                                    }
                                                }), text = "翻译", fontSize = 10.sp)
                                            }
                                            else{
                                                Text(modifier = Modifier.clickable(onClick = {
                                                    ifShow.value = !ifShow.value
                                                    replyContent.value = it.content
                                                }), text = "x", fontSize = 10.sp)
                                            }
                                        }

                                    }

                                }
                            }
                        }
                    }
                    if (comment.comments.size > index.intValue) {
                        TextButton(onClick = {
                            index.intValue =
                                if (index.intValue + 2 > comment.comments.size) comment.comments.size else index.intValue + 2
                            Log.d(
                                "CommentItem",
                                "index.intValue: ${index.intValue}, comment.comments.size: ${comment.comments.size}"
                            )
                            replyList.clear()
                            replyList.addAll(comment.comments.subList(0, index.intValue))
                        }) {
                            Row {
                                Text("更多评论")
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "展开更多评论",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
