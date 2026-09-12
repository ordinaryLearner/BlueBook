package com.czcz.myapp.Api


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import com.czcz.myapp.Api.Models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @GET("health")
    fun health(): Call<HealthResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<CallBackData>>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<CallBackData>>
    @POST("api/auth/me")
    fun getMe(@Body request: GetMeRequest): Call<ApiResponse<CallBackData>>
    @GET("api/users/{id}")
    fun getUser(@Path("id") id: String, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @POST("api/users/{id}/follow")
    fun follow(@Path("id") id: String, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @DELETE("api/users/{id}/unfollow")
    fun unfollow(@Path("id") id: String, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @POST("api/auth/auto_login")
    fun autologin(@Body request: AutoLoginRequest): Call<ApiResponse<CallBackData>>

    @POST("api/posts")
    fun post(@Body request: PublishRequest, @Header ("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @Multipart
    @POST("api/posts/video")
    fun postVideo(
        @Part video: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Header("Authorization") token: String
    ): Call<ApiResponse<Post>>

    @POST("api/posts/random")
    fun getRandomPosts(@Body request: RandomPostRequest): Call<ApiResponse<List<Post>>>

    @GET("api/posts/my")
    fun getMyPosts(@Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
    @GET("api/posts/myliked")
    fun getMyLikedPosts(@Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
    @GET("api/posts/myfavorites")
    fun getFavouritePosts(@Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
    @POST("api/posts/history")
    fun getHistoryPost(@Body request: HistoryRequest,@Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
    @GET("api/posts/user/{userId}")
    fun getUserPosts(@Path("userId") userId: String, @Header("Authorization")token: String): Call<ApiResponse<List<Post>>>
    @POST("api/posts/search")
    fun search(@Body request: SearchRequest): Call<ApiResponse<List<Post>>>

    @POST("api/comments")
    fun comment(@Body request: CommentRequest, @Header("Authorization")token: String): Call<ApiResponse<CommentCallBack>>
    @PUT("api/users/profile")
    fun updateProfile(@Body request: UpdateProfileRequest, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @POST("api/likes")
    fun like(@Body request: LikeRequest): Call<ApiResponse<CallBackData>>
    @POST("api/likes/unlike")
    fun dislike(@Body request: LikeRequest): Call<ApiResponse<CallBackData>>
    @POST("api/posts/{postId}/favorite")
    fun favorite(@Path("postId") postId: String, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @DELETE("api/posts/{postId}/favorite")
    fun unfavorite(@Path("postId") postId: String, @Header("Authorization")token: String): Call<ApiResponse<CallBackData>>
    @POST("api/translate")
    fun translate(@Body request: TranslateRequest): Call<ApiResponse<TranslateCallBack>>
    @POST("api/messages")
    fun sendMessage(@Body request: MessageRequest, @Header("Authorization")token: String): Call<ApiResponse<Message>>
    @POST("api/messages/conversation-id")
    fun getConversationId(@Body request: ConversationIdRequest): Call<ApiResponse<ConversationId>>
    @GET("api/messages/unread")
    fun getUnreadMessages(@Header("Authorization")token: String): Call<ApiResponse<List<Message>>>
    @GET("api/messages/conversation/{userId}")
    fun getConversationMessages(
        @Path("userId") userId: String,
        @Header("Authorization") token: String
    ): Call<ApiResponse<List<Message>>>
}

interface ImgBBService {
    @FormUrlEncoded
    @POST("upload")
    fun uploadImage(
        @Query("key") key: String,
        @Field("image") image: String
    ): Call<ImgBBResponse>
}
