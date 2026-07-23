package com.czcz.myapp

import androidx.compose.ui.graphics.vector.ImageVector

class Models {
    data class User(
        val username: String?=null,
        val account: String,
        val password: String,
        val joinTime: String,
        val followers:Int,
        val likes:Int,
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
}