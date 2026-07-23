package com.czcz.myapp

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.Flow

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: String?
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
    val token:String
)

data class CallBackData(
    val user: UserInfo?,
    val token: String?
)

data class UserInfo(
    val id: String?,
    val account: String?,
    val username: String?,
   val avatar: String?,
    val bio: String?,
    @SerializedName("join_time") val joinTime: String?
)

data class HealthResponse(
    val status: String?,
    val timestamp: String?
)
