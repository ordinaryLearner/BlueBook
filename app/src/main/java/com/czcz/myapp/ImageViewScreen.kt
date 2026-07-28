package com.czcz.myapp

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun ImageViewScreen(navController: NavController,viewModel: ViewModel) {
    val checkImageList = viewModel.checkImageList.collectAsState()
    val currentIndex = viewModel.currentIndex.collectAsState()
    val PagerState = rememberPagerState(pageCount =  { checkImageList.value.size }, initialPage = currentIndex.value)

    Box(){
        HorizontalPager(state = PagerState,modifier = Modifier.fillMaxSize()){
                page ->

            AsyncImage(
                model = checkImageList.value[page],
                contentDescription = "图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            TextButton(onClick = {}, modifier = Modifier.padding(8.dp)) { Text("${PagerState.currentPage + 1} / ${checkImageList.value.size}")
            }
            IconButton(onClick = {navController.popBackStack()},
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "选择图片或视频",
                    modifier = Modifier.size(25.dp)
                )
            }
        }


    }
}