package com.czcz.myapp.Room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.czcz.myapp.Api.Models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "history")
data class History(
    @PrimaryKey
    val id: String,
    val time: String,
    val userId:String
){

    fun getTimeLabel(): String {
        if (time.isEmpty()) return ""

        return try {
            val timestamp = time.toLong()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())


            val timeInMillis = if (timestamp > 1000000000000L) timestamp else timestamp * 1000

            val targetDate = sdf.format(Date(timeInMillis))
            val today = sdf.format(Date())
            val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000L))

            when (targetDate) {
                today -> "今天"
                yesterday -> "昨天"
                else -> {
                    val target = Date(timeInMillis)
                    val weekAgo = Date(System.currentTimeMillis() - 6 * 86400000L)
                    if (target.after(weekAgo)) "一周前" else "很久前"
                }
            }
        } catch (e: Exception) {
            time  // 出错返回原值
        }
    }
}
@Entity(tableName = "conversation")
data class Conversation(
    @PrimaryKey
    val id: String,
    val partner: User,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val ownerId: String,
)
@Entity(tableName = "notification")
data class Notification(
    @PrimaryKey
    val id: String,
    val time: String,
    val senderId: String,
    val userId: String,
    val type: NotificationType,
)
enum class NotificationType {
    LIKE, COMMENT, FOLLOW,FAVOURITE,MESSAGE
}
