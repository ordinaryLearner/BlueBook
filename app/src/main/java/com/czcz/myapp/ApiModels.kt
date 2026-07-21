package com.czcz.myapp

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?,
    @SerializedName("timestamp") val timestamp: String?
)

data class RegisterRequest(
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String,
    @SerializedName("username") val username: String? = null
)

data class LoginRequest(
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String
)

data class RegisterData(
    @SerializedName("user") val user: UserInfo?,
    @SerializedName("token") val token: String?
)

data class UserInfo(
    @SerializedName("id") val id: String?,
    @SerializedName("account") val account: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("join_time") val joinTime: String?
)

data class HealthResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("timestamp") val timestamp: String?
)
