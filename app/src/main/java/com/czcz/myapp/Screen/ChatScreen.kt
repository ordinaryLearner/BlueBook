package com.czcz.myapp.Screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.Api.LoginViewModel
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.ui.Avatar
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.UserViewModel
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.Room.PostDatabase
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, messageViewModel: MessageViewModel, infoViewModel: InfoViewModel, postViewModel: PostViewModel, loginViewModel: LoginViewModel, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val errorMessage by messageViewModel.errorMessage.collectAsState(initial = "")
    val followErrorMessage by userViewModel.errorMessage.collectAsState(initial = "")
    val user by DataStorePreference.getUser(context).collectAsState(initial = User.empty())
    val currentConversationId by messageViewModel.currentConversationId.collectAsState()
    val messageList = messageViewModel.messageList.collectAsState().value
    val partner by messageViewModel.currentPartner.collectAsState()
    val contentInput by messageViewModel.contentInput.collectAsState()
    val isInputing by messageViewModel.isPutting.collectAsState()
    val isLoading by messageViewModel.isLoading.collectAsState()
    var ifFollowed = loginViewModel.followersList.collectAsState().value.any { it == partner.id }
    val animatedWidth by animateDpAsState(
        targetValue = if (ifFollowed) 70.dp else 30.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    val animatedFollowColor by animateColorAsState(
        targetValue = if (ifFollowed) Color(0xFF999999) else Color(0xFFFF4D6A),
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 1000f)
    )
    LaunchedEffect(currentConversationId) {
        if (currentConversationId.isNotEmpty()) {
            messageViewModel.loadMessages(context)
            messageViewModel.startConversationPolling(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose { messageViewModel.stopConversationPolling() }
    }

    LaunchedEffect(currentConversationId){
        if(currentConversationId.isNotEmpty()){
            messageViewModel.loadMessages(context)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            messageViewModel.resetErrorMessage()
        }
    }

    LaunchedEffect(followErrorMessage) {
        if (followErrorMessage.isNotEmpty()) {
            Toast.makeText(context, followErrorMessage, Toast.LENGTH_SHORT).show()
            userViewModel.resetErrorMessage()
        }
    }
    Scaffold(
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
                    value = contentInput,
                    onValueChange = {
                        messageViewModel.setIsPutting(true)
                        messageViewModel.setContentInput(it)
                    },
                    enabled = !isLoading,
                    label = { Text("说点什么") }
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
                                    messageViewModel.sendMessage(context)
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
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(){
                        Avatar(
                            url = partner.avatar,
                            size = 32.dp,
                            modifier = Modifier.clickable {
                                infoViewModel.getDetailUser(context, partner.id)
                                postViewModel.getUserPost(context, partner.id)
                                navController.navigate("UserDetailScreen")
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = partner.username ?: "BB用户",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable {
                                infoViewModel.getDetailUser(context, partner.id)
                                postViewModel.getUserPost(context, partner.id)
                                navController.navigate("UserDetailScreen")
                            }
                        )
                        OutlinedButton(
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                if (partner.id != user.id) {
                                    if (ifFollowed) loginViewModel.deleteFollower(partner.id)
                                    else loginViewModel.addFollower(partner.id)
                                    ifFollowed = !ifFollowed
                                    userViewModel.followUser(
                                        context,
                                        partner.id,
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
                },
                navigationIcon = {
                    Row(modifier = Modifier.padding(16.dp)){
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.Black,
                            modifier = Modifier.clickable{
                                navController.popBackStack()
                            }
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(modifier = Modifier
            .padding(it)
            .fillMaxSize()
                ,reverseLayout = true){
            items(messageList.asReversed()) { message ->
                MessageItem(message)
            }
        }
    }
}
@Composable
fun MessageItem(message:Message){
    val context = LocalContext.current
    val user by DataStorePreference.getUser(context).collectAsState(User.empty())
    Row(){
        if(message.sender.id != user.id){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(
                    url = message.sender.avatar ?: "BB用户",
                    size = 40.dp,
                    modifier = Modifier.padding(8.dp)
                )
                Column() {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.Black
                        )
                    }
                    Text(text = message.time, color = Color.Gray, fontSize = 6.sp)
                }
            }
        }
        else{
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ){
                Column() {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Color.Black
                        )
                    }
                    Text(text = message.time, color = Color.Gray, fontSize = 6.sp)
                }
                Avatar(
                    url = message.sender.avatar ?: "BB用户",
                    size = 40.dp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}