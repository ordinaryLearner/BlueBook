package com.czcz.myapp

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preference")

object DataStorePreference{
    private val TOKEN = stringPreferencesKey("token")
    private val USER_ID = stringPreferencesKey("user_id")
    private val KEY_ACCOUNT = stringPreferencesKey("account")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_AVATAR = stringPreferencesKey("avatar")
    private val KEY_BIO = stringPreferencesKey("bio")
    private val KEY_AUTOLOGIN = booleanPreferencesKey("autologin")

    suspend fun saveUserInfo(token: String,context: Context, autoLogin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
//            preferences[USER_ID] = user.id
//            preferences[KEY_ACCOUNT] = user.account
//            preferences[KEY_USERNAME] = user.username
//            preferences[KEY_AVATAR] = user.avatar
//            preferences[KEY_BIO] = user.bio
            preferences[KEY_AUTOLOGIN] = autoLogin
        }
    }
    fun getAutoLogin(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_AUTOLOGIN] ?: false
        }
    }
    fun getToken(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }
    }
}