package com.czcz.myapp.Room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.czcz.myapp.Api.Models.*


@Database(entities = [History::class, Post::class,Message::class,Conversation::class], version = 1)
@TypeConverters(Converters::class)
abstract class PostDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao

    companion object {

        private var instance: PostDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): PostDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                PostDatabase::class.java, "room_database"
            )
                .build().apply {
                    instance = this
                }
        }
    }
}