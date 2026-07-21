package com.czcz.myapp

class Models {
    data class User(
        val username: String?=null,
        val account: String,
        val password: String,
        val joinTime: String,
        val followers:Int,
        val likes:Int,
        val posts: MutableList<Post>?= mutableListOf(),
        val likedPost: MutableList<Post>?= mutableListOf()
    )

    data class Post(
        val sender:User,
        val likes:Int,
        val comments: MutableList<Comment>?= mutableListOf()
    )

    data class Comment(
        val sender:User,
        val likes:Int,
        val comments: MutableList<Comment>?= mutableListOf()
    )
    data class FeedItem(
        val id: String,
        val imageUrl: String,
        val title: String,
        val likeCount: Int,
        val isLiked: Boolean = false
    )
}