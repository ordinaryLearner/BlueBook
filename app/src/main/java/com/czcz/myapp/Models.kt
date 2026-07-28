package com.czcz.myapp

import androidx.compose.ui.graphics.vector.ImageVector



    data class User(
        val id: String,
        val username: String?=null,
        val account: String,
        val password: String,
        val avatar: String,
        val joinTime: String,
        val followers:Int?=0,
        val likes:Int?=0,
        //val posts: MutableList<Post>?= mutableListOf(),
        //val likedPost: MutableList<Post>?= mutableListOf()
    )

    data class Post(
        val id: String,
        val media:Media,
        val sender:User,
        val likes:Int,
        val comments: MutableList<Comment>?= mutableListOf()
    )

    data class Comment(
        val id: String,
        val sender:User,
        val likes:Int,
        val comments: MutableList<Comment>?= mutableListOf()
    )
    data class BottomNavItem(
        val title: String,
        val icon: ImageVector
    )
    data class Media(
        val type: MediaType,
        val url: String
    )
    enum class MediaType {
        IMAGE,
        VIDEO
    }
