package com.czcz.myapp.Api

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class Models {
    data class User(
        val id: String,
        val username: String? = null,
        val account: String,
        val avatar: String? = null,
        val background: String? = null,
        val bio: String? = null,
        @SerializedName("join_time") val joinTime: String,
        val followers: MutableList<String>? = mutableListOf(),
        val fans: MutableList<String>? = mutableListOf(),
        @SerializedName("totalLikes") val likes: Int = 0,
    ) {
        fun toTimestamp(): Long {
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                format.parse(joinTime)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        companion object {
            fun empty() = User("", "", "", "", "", "", "", mutableListOf(), mutableListOf())
        }
    }

    @Entity(tableName = "post")
    data class Post(
        @PrimaryKey
        val id: String,
        val title: String,
        val content: String,
        val time: String,
        val medias: MutableList<Media> = mutableListOf(),
        val sender: User,
        val likes: MutableList<String> = mutableListOf(),
        val favourite: MutableList<String> = mutableListOf(),
        val comments: MutableList<Comment>? = mutableListOf()
    ) {
        companion object {
            fun empty() = Post(
                "",
                "",
                "",
                "",
                mutableListOf(),
                User("", "", "", "", "", "", "", mutableListOf(), mutableListOf()),
                mutableListOf(),
                mutableListOf(),
                mutableListOf(),

                )
        }
    }

    data class Comment(
        val id: String,
        val content: String,
        val time: String,
        val type: CommentType,
        val sender: User,
        val receiver: User? = null,
        val likes: MutableList<String>,
        val comments: MutableList<Comment>? = mutableListOf()
    ) {
        companion object {
            fun empty() = Comment(
                "",
                "",
                "",
                CommentType.POSTCOMMENT,
                User("", "", "", "", "", "", "", mutableListOf(), mutableListOf()),
                null,
                mutableListOf(),
                mutableListOf()
            )
        }
    }

    data class BottomNavItem(
        val title: String,
        val icon: ImageVector
    )

    data class Media(
        val type: MediaType,
        val url: String
    )

    enum class PublishType {
        IMAGEPOST, VIDEOPOST
    }


    enum class MediaType {
        IMAGE,
        VIDEO
    }

    enum class CommentType {
        POSTCOMMENT,
        REPLYCOMMENT
    }

    data class ApiResponse<T>(
        val code: Int,
        val message: String,
        val data: T?,
        //val timestamp: String?
    )

    data class HistoryRequest(
        val postIds: List<String>? = null
    )

    data class GetMeRequest(
        val token: String
    )

    data class TranslateRequest(
        val text: String,
    )

    data class RegisterRequest(
        val account: String,
        val password: String,
        val username: String? = null
    )

    data class LoginRequest(
        val account: String,
        val password: String
    )

    data class AutoLoginRequest(
        val account: String,
        val token: String
    )

    data class PublishRequest(
        val senderId: String,
        val title: String,
        val content: String,
        val images: List<String>? = null,
    )

    data class CommentRequest(
        val senderId: String,
        val commentType: CommentType,
        val commentId: String,
        val receiverId: String? = null,
        val postId: String,
        val content: String
    )

    data class LikeRequest(
        val userId: String,
        val postId: String,
        val commentId: String? = null,
        val type: LikeType
    )

    data class FavoriteRequest(
        val userId: String,
        val postId: String,
        val commentId: String? = null,
        val type: LikeType
    )

    enum class LikeType {
        POSTLIKE,
        COMMENTLIKE
    }

    data class UpdateProfileRequest(
        val username: String? = null,
        val avatar: String? = null,
        val bio: String? = null,
        val background: String? = null
    )

    data class RandomPostRequest(
        val excludeIds: List<String>? = null,
        val type: MediaType
    )

    data class SearchRequest(
        val keyword: String,
        val excludePosts: List<String>? = null,
        val page: Int? = null,
        val pageSize: Int? = null
    )

    data class MessageRequest(
        val id: String? = null,
        val receiverId:String,
        val text: String,
        val type: MessageType? = MessageType.TEXT,
        val postId: String? = null
    )
    data class ConversationId(
        val conversationId: String
    )

    data class ConversationIdRequest(
        val userId:String,
        val otherId:String
    )

    data class CallBackData(
        val user: User,
        val token: String?
    )

    data class TranslateCallBack(
        val translation: String
    )

    data class CommentCallBack(
        val id: String,
        val content: String,
        val time: String,
        val type: CommentType,
        val sender: User,
        val receiver: User?,
        val likes: MutableList<User>,
        val comments: MutableList<Comment>?
    )


    data class HealthResponse(
        val status: String?,
        val timestamp: String?
    )

    data class ImgBBResponse(
        val data: ImgBBData?,
        val success: Boolean,
        val status: Int
    )

    data class ImgBBData(
        val url: String?,
        @SerializedName("display_url") val displayUrl: String?,
        @SerializedName("delete_url") val deleteUrl: String?
    )

    @Entity(tableName = "message")
    data class Message(
        @PrimaryKey
        val id: String,
        val conversationId: String,
        val postId: String? = null,
        val sender: User,
        val receiver: User,
        val text: String,
        val time: String,
        val isRead: Boolean,
        val type: MessageType
    )

    enum class MessageType {
        TEXT,
        POST,
        COMMENT
    }

    enum class PostType {
        LIKE, FAVOURITE, MINE
    }
}