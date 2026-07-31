package com.czcz.myapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("health")
    fun health(): Call<HealthResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<CallBackData>>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<CallBackData>>
    @POST("api/auth/me")
    fun autologin(@Body request: AutoLoginRequest): Call<ApiResponse<CallBackData>>

    @POST("api/posts")
    fun post(@Body request: PublishRequest,@Header ("Authorization")token: String): Call<ApiResponse<CallBackData>>

    @GET("api/posts/random")
    fun getRandomPosts(): Call<ApiResponse<List<Post>>>

    @GET("api/posts/my")
    fun getMyPosts(@Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
}

interface ImgBBService {
    @FormUrlEncoded
    @POST("upload")
    fun uploadImage(
        @Query("key") key: String,
        @Field("image") image: String
    ): Call<ImgBBResponse>
}
