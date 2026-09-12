package com.czcz.myapp.Room

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.czcz.myapp.Api.Models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)
    @Insert
    suspend fun insertPost(post: Post)
    @Delete
    suspend fun deletePost(post: Post)
    @Query("SELECT * FROM post")
    suspend fun getAllPosts(): List<Post>
    @Query("DELETE FROM post")
    suspend fun deleteAllPosts()
    @Query("SELECT id FROM post")
    suspend fun getAllPostIds(): List<String>
    @Query("SELECT * FROM post ORDER BY time DESC")
    fun getAllPostsFlow(): Flow<List<Post>>


    @Query("SELECT * FROM history WHERE id = :id")
    suspend fun getHistoryById(id: String): History?
    @Insert
    suspend fun insertHistory(history: History)
    @Delete
    suspend fun deleteHistory(history: History)
    @Query("SELECT * FROM history")
    suspend fun getAllHistory(): List<History>
    suspend fun getAllHistoryByUserId(userId: String): List<History> {
        Log.d("getAllHistoryByUserId", "userId: $userId")
        return getAllHistory().filter { it.userId == userId }
    }
    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
    @Query("SELECT * FROM post WHERE id = :id")
    suspend fun getPostById(id: String): Post

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    //批量添加message
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    @Query("SELECT * FROM message WHERE conversationId = :conversationId")
    suspend fun getMessagesByConservationId(conversationId: String): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)
    //更新会话
    @Update()
    suspend fun updateConversation(conversation: Conversation)
    //按照ownerid获取会话
    @Query("SELECT * FROM conversation WHERE ownerId = :ownerId")
    suspend fun getConversationByOwnerId(ownerId: String): List<Conversation>?
    //通过id获取会话
    @Query("SELECT * FROM conversation WHERE id = :id")
    suspend fun getConversationById(id: String): Conversation?
    @Query("SELECT * FROM conversation")
    suspend fun getAllConversations(): List<Conversation>
}