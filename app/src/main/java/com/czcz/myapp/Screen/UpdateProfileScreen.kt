package com.czcz.myapp.Screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.czcz.myapp.Api.InfoViewModel
import com.czcz.myapp.ui.Avatar
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.ui.theme.skyBlueDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProfileScreen(infoViewModel: InfoViewModel,navController: NavController) {
    val context = LocalContext.current
    val user = DataStorePreference.getUser(context).collectAsState(User.empty()).value
    val isLoading by infoViewModel.isLoading.collectAsState()
    val isSuccess by infoViewModel.isSuccess.collectAsState()
    val newUsername by infoViewModel.newUsername.collectAsState(user.username?:"")
    val newBio by infoViewModel.newBio.collectAsState(user.bio?:"")
    val newAvatar by infoViewModel.newAvatar.collectAsState(user.avatar?:"")
    val errorMessage by infoViewModel.errorMessage.collectAsState("")
    val pickAvatar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            if (it != null) {
                infoViewModel.setNewAvatar(it)
                infoViewModel.updateProfile(context)
            }
        }
    )

    LaunchedEffect(errorMessage,isSuccess) {
        if (errorMessage != "") {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            infoViewModel.resetErrorMessage()
        }
        if(isSuccess) {
            navController.popBackStack()
            infoViewModel.resetIsSuccess()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改个人信息") },
                navigationIcon = {
                    IconButton(onClick = {
                        infoViewModel.setIsUpdateProfile(false)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()){
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        infoViewModel.setIsUpdateProfile(false)
                        navController.popBackStack()
                    }
                ) {
                    Text("取消")
                }
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        infoViewModel.updateProfile(context)
                    }
                ) {
                    if (isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = skyBlueDark,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "更新中...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "确认",
                            maxLines = 1,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(16.dp))
            Avatar(
                url = user.avatar,
                size = 80.dp,
                modifier = Modifier.clickable {
                    pickAvatar.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            Text(text = "点击更换头像", fontSize = 12.sp)
            Spacer(modifier = Modifier.padding(16.dp))
            TextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                enabled = !isLoading,
                value = newUsername,
                onValueChange = { infoViewModel.setNewUsername(it) },
                label = { Text("修改用户名称") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent
                ),
                enabled = !isLoading,
                value = newBio,
                onValueChange = { infoViewModel.setNewBio(it) },
                label = { Text("输入新的简介") })
        }
    }
}