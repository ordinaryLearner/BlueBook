package com.czcz.myapp.Screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.czcz.myapp.Api.PostViewModel
import com.czcz.myapp.ui.theme.skyBlue
import com.czcz.myapp.ui.theme.skyBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPublishScreen(navController: NavController, postViewModel: PostViewModel) {
    val context = LocalContext.current
    val title = postViewModel.titleEdit.collectAsState()
    val content = postViewModel.contentEdit.collectAsState()
    val isLoading by postViewModel.isLoading.collectAsState()
    val ifPost by postViewModel.ifPost.collectAsState()
    val errorMessage by postViewModel.errorMessage.collectAsState()
    val videoUri by postViewModel.videoUri.collectAsState()
    val videoEnabledLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
    val request = ImageRequest.Builder(context)
        .data(videoUri)
        .videoFrameMillis(1000)
        .build()
    LaunchedEffect(Unit) {
        postViewModel.resetPublishState()
    }
    LaunchedEffect(ifPost) {
        if (ifPost) {
            Toast.makeText(context, "发布成功", Toast.LENGTH_SHORT).show()
            postViewModel.dissaved()
            navController.popBackStack()
            postViewModel.resetPublishState()
        }
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            postViewModel.resetPublishState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "让更多人与你相连",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = request,
                    imageLoader = videoEnabledLoader,
                    contentDescription = "视频封面",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title.value,
                enabled = !isLoading,
                onValueChange = { postViewModel.setTitleEdit(it) },
                placeholder = {
                    Text(
                        text = "请输入标题",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = skyBlue,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = skyBlueDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content.value,
                enabled = !isLoading,
                onValueChange = { postViewModel.setContentEdit(it) },
                placeholder = {
                    Text(
                        text = "分享你的故事...",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 10.sp
                ),
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = skyBlue,
                    unfocusedBorderColor = Color.LightGray,
                    cursorColor = skyBlueDark
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                enabled = !isLoading,
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = "取消",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                enabled = !isLoading,
                onClick = {
                    postViewModel.postVideo(context)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = skyBlue,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = skyBlueDark,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "发布中...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "确认",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
