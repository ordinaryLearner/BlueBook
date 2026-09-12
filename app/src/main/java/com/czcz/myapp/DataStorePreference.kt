package com.czcz.myapp

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.czcz.myapp.Api.Models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preference")

object DataStorePreference{
    private val TOKEN = stringPreferencesKey("token")
    private val USER_ID = stringPreferencesKey("user_id")
    private val KEY_ACCOUNT = stringPreferencesKey("account")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_PASSWORD = stringPreferencesKey("password")
    private val KEY_JOIN_TIME = stringPreferencesKey("join_time")
    private val KEY_LIKES = intPreferencesKey("likes")

    private val KEY_AVATAR = stringPreferencesKey("avatar")
    private val KEY_BIO = stringPreferencesKey("bio")
    private val KEY_BACKGROUND = stringPreferencesKey("background")
    private val KEY_AUTOLOGIN = booleanPreferencesKey("autologin")




    private const val DEFAULT_USERNAME = "BB用户"



    suspend fun saveUserInfo(token: String,context: Context, autoLogin: Boolean, data: User) {
            context.dataStore.edit { preferences ->
                preferences[TOKEN] = token
                preferences[USER_ID] = data.id
                preferences[KEY_ACCOUNT] = data.account
                preferences[KEY_USERNAME] = data.username ?: DEFAULT_USERNAME
                preferences[KEY_BACKGROUND] = data.background ?: ""
                preferences[KEY_AVATAR] = data.avatar ?: ""
                preferences[KEY_BIO] = data.bio ?: ""
                preferences[KEY_JOIN_TIME] = data.joinTime
                preferences[KEY_AUTOLOGIN] = autoLogin
                preferences[KEY_LIKES] = data.likes
            }
    }
    suspend fun clearData(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    fun getAutoLogin(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_AUTOLOGIN] ?: false
        }
    }
    fun getAccount(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_ACCOUNT] ?: ""
        }
    }
    fun getPassword(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_PASSWORD] ?: ""
        }
    }
    fun getToken(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }
    }
    fun getUser(context: Context): Flow<User> {
        return context.dataStore.data.map { preferences ->
            Log.d("DataStorePreference", "getUser: ${preferences[KEY_LIKES]}")
            User(
                id = preferences[USER_ID] ?: "",
                account = preferences[KEY_ACCOUNT] ?: "",
                username = preferences[KEY_USERNAME] ?: DEFAULT_USERNAME,
                avatar = preferences[KEY_AVATAR] ?: "",
                background = preferences[KEY_BACKGROUND] ?: "",
                bio = preferences[KEY_BIO] ?: "Your bio",
                joinTime = preferences[KEY_JOIN_TIME] ?: "",
                likes = preferences[KEY_LIKES] ?: 0
            )
        }
    }

}