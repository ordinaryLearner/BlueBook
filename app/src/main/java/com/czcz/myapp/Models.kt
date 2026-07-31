package com.czcz.myapp

import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import java.sql.Time


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
    ){
        companion object{
            fun empty() = User("","","","","","")
        }
    }

    data class Post(
        val id: String,
        val title: String,
        val content: String,
        val medias: MutableList<Media>,
        val sender:User,
        val likes: MutableList<String>,
        val comments: MutableList<Comment>?= mutableListOf()
    ){
        companion object{
            fun empty() = Post("", "","",mutableListOf(), User("","","","","",""), mutableListOf(), mutableListOf())
        }
    }

    data class Comment(
        val id: String,
        val content: String,
        val time: Time,
        val sender:User,
        val likes:Int,
        //val comments: MutableList<Comment>?= mutableListOf()
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
