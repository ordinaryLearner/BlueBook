package com.czcz.myapp.Room

import androidx.room.TypeConverter
import com.czcz.myapp.Api.Models
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonParseException
import com.czcz.myapp.Api.Models.*

class Converters {
    private val gson = Gson()


    @TypeConverter
    fun fromMediaList(list: MutableList<Media>?): String {
        return if (list.isNullOrEmpty()) "[]" else gson.toJson(list)
    }

    @TypeConverter
    fun toMediaList(json: String?): MutableList<Media> {
        if (json.isNullOrBlank() || json == "[]" || json == "null") {
            return mutableListOf()
        }
        val type = object : TypeToken<MutableList<Media>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }


    @TypeConverter
    fun fromStringList(list: MutableList<String>?): String {
        return if (list.isNullOrEmpty()) "[]" else gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): MutableList<String> {
        if (json.isNullOrBlank() || json == "[]" || json == "null") {
            return mutableListOf()
        }
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }


    @TypeConverter
    fun fromCommentList(list: MutableList<Comment>?): String {
        return if (list.isNullOrEmpty()) "[]" else gson.toJson(list)
    }

    @TypeConverter
    fun toCommentList(json: String?): MutableList<Comment> {
        if (json.isNullOrBlank() || json == "[]" || json == "null") {
            return mutableListOf()
        }
        val type = object : TypeToken<MutableList<Comment>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }


    @TypeConverter
    fun fromUser(user: User?): String {
        return if (user == null) "{}" else gson.toJson(user)
    }

    @TypeConverter
    fun toUser(json: String?): User {
        if (json.isNullOrBlank() || json == "null" || json == "{}") {
            return User.empty()
        }
        return gson.fromJson(json, User::class.java) ?: User.empty()
    }
}