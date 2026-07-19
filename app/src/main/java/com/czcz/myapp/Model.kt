package com.czcz.myapp

class Model {
    data class User(
        val account: Int,
        val name: String,
        val email: String,
        val password: String,
        val isAuto: Boolean?=null,
        val profilePicture: Int,
        val postCount: Int,
        val likeCount: Int,
        val introduction: String,
        val posts: MutableList<Post>,
        val likes: MutableList<Post>
    )

    data class Post(
        val sender: User,
        val mediaList: MutableList<Int>,
        val title: String,
        val text: String,
        val likes: Int,
        val comments: MutableList<Comment>,
        val postedTime: String,
    )

    data class Comment(
        val sender: User,
        val text: String,
        val postedTime: String,
    )

    data class Message(
        val sender: User,
        val receiver: User,
        val text: String,
        val postedTime: String,
    )
}