package com.czcz.myapp.Screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.czcz.myapp.Api.PostViewModel

@Composable
fun VideoViewScreen(navController: NavController, postViewModel: PostViewModel) {
    Box(modifier = Modifier.fillMaxSize()){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current
            val uri = postViewModel.videoUri.collectAsState()
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(uri.value))
                    prepare()
                    playWhenReady = true
                    repeatMode = ExoPlayer.REPEAT_MODE_ONE
                }
            }

            DisposableEffect(Unit) {
                onDispose { exoPlayer.release() }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                }
            )
        }
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically){
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.padding(end = 100.dp).clickable { navController.popBackStack() }
            )
            TextButton(
                //shape = RoundedCornerShape(5.dp),
                //modifier = Modifier.background(Color.White.copy(alpha = 0.5f)),
                onClick = {}
            ) {
                Text("正在预览")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween){
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                }
            )
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.clickable {
                    navController.popBackStack()
                    navController.navigate("VideoPublishScreen")
                }
            )
        }
    }
}