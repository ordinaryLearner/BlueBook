package com.czcz.myapp

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(navController: NavController,viewModel: ViewModel) {
    val skyBlue = Color(0xFF87CEEB)
    val skyBlueDark = Color(0xFF5BB0D9)
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var ifSaved by remember { mutableStateOf(false) }
    val uriList = viewModel.uriList.collectAsState()
    val pickMultipleMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8),
        onResult = {
            viewModel.setUriList(it)
        }
    )

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
                    if(viewModel.uriList.value.isNotEmpty() || title.isNotEmpty() || content.isNotEmpty()){ ifSaved = true }
                    else navController.popBackStack()
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
        if (ifSaved) {
            AlertDialog(
                onDismissRequest = { ifSaved = false },
                title = { Text("退出") },
                text = { Text("是否保存内容？") },
                confirmButton = {
                    Button(
                        onClick = {
                            ifSaved = false
                            viewModel.dissaved()
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White
                        )
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { ifSaved = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("取消")
                    }
                }
            )
        }

        //Divider(color = Color.LightGray.copy(alpha = 0.5f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {


                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f)
                ) {
                    items(uriList.value.size + 1) { index ->
                        if (index < uriList.value.size) {
                            Box(
                                modifier = Modifier
                                    .size(95.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .clickable {
                                        navController.navigate("ImageViewScreen")
                                        viewModel.checkImage(uriList.value, index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = uriList.value[index],
                                    contentDescription = "展示照片或视频"
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(95.dp)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0E0E0))
                                    .clickable {
                                        pickMultipleMedia.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "选择图片或视频",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }


            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
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
                value = content,
                onValueChange = { content = it },
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

        //Divider(color = Color.LightGray.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
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
                onClick = { },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = skyBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "确认",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
