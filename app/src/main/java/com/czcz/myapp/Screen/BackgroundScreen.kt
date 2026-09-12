package com.czcz.myapp.Screen


import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import com.czcz.myapp.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.czcz.myapp.Api.InfoViewModel

@Composable
fun BackgroundScreen(navController: NavController,infoViewModel: InfoViewModel) {
    val context = LocalContext.current
    val background by infoViewModel.currentBackground.collectAsState()
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            if (it != null) {
                infoViewModel.setNewBackground(it)
                infoViewModel.setOnlyUpdateBackground(true)
                infoViewModel.updateProfile(context)
            }
        }
    )
    Box{
        AsyncImage(
            model = if(background.toString() != "") background else R.drawable.default_background,
            contentDescription = "背景图片",
            modifier = Modifier.fillMaxSize(),
        )
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "返回",
                modifier = Modifier.size(25.dp)
            )
        }
        TextButton(
            onClick = {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            modifier = Modifier.padding(8.dp).align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.5F))
        ) {Text("更改背景图片", color = Color.White) }
    }
}
