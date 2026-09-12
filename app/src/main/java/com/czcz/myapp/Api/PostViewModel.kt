package com.czcz.myapp.Api

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.annotation.MainThread
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.czcz.myapp.DataStorePreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Room.History
import com.czcz.myapp.Room.PostDatabase
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import com.czcz.myapp.Api.Models.MediaType.*
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class PostViewModel : ViewModel() {

    private val _gridState = MutableStateFlow(LazyStaggeredGridState())
    val gridState: StateFlow<LazyStaggeredGridState> = _gridState
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
    fun resetErrorMessage() {
        _errorMessage.value = ""
    }
    private val _postList = MutableStateFlow<MutableList<Post>>(mutableListOf())
    val postList: StateFlow<List<Post>> = _postList

    private val _currentPost = MutableStateFlow<Post>(Post.Companion.empty())
    val currentPost: StateFlow<Post> = _currentPost
    fun setCurrentPost(post: Post) {
        _currentPost.value = post
    }

    private val _myPostList = MutableStateFlow<MutableList<Post>>(mutableListOf())
    val myPostList: StateFlow<List<Post>> = _myPostList
    private val _likedPostList = MutableStateFlow<MutableList<Post>>(mutableListOf())
    val likedPostList: StateFlow<List<Post>> = _likedPostList


    object ApiClient {
        private const val BASE_URL = "https://bluebook-backend-at73.onrender.com/"
        private const val IMGBB_BASE_URL = "https://api.imgbb.com/1/"
        const val IMGBB_API_KEY = "d35841f781c7eb9c8bd4f0e6f6d00b6a"
        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)   // 连接超时
            .readTimeout(15, TimeUnit.SECONDS)      // 读取超时
            .writeTimeout(15, TimeUnit.SECONDS)     // 写入超时
            .callTimeout(30, TimeUnit.SECONDS)      // 整个请求的总超时
            .build()
        private val uploadOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .build()
        private val uploadRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(uploadOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val imgbbRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(IMGBB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
        val uploadService: ApiService = uploadRetrofit.create(ApiService::class.java)

        val imgbbService: ImgBBService = imgbbRetrofit.create(ImgBBService::class.java)
    }

//    private val _postList = MutableStateFlow<MutableList<Post>>(mutableListOf())
//    val postList: StateFlow<List<Post>> = _postList

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    private val _isLoadMore = MutableStateFlow(false)
    val isLoadMore: StateFlow<Boolean> = _isLoadMore
    fun setIsLoadMore(value: Boolean) {
        _isLoadMore.value = value
    }
    private val _videoPostList = MutableStateFlow<MutableList<Post>>(mutableListOf())
    val videoPostList: StateFlow<List<Post>> = _videoPostList

    private val _videoPostFile = MutableStateFlow<MutableList<File>>(mutableListOf())
    val videoPostFile: StateFlow<List<File>> = _videoPostFile
    fun transformVideoPostFile(context: Context) {
        val files = _videoPostList.value.map { post ->
            File(context.cacheDir, post.id)
        }
        _videoPostFile.value = files.toMutableList()
    }

    private val bg = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    fun getVideoPost(context: Context){
        val dao = PostDatabase.getDatabase(context).postDao()
        val call = ApiClient.apiService.getRandomPosts(RandomPostRequest(null,VIDEO))
        call.enqueue(object : Callback<ApiResponse<List<Post>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Post>>>,
                response: Response<ApiResponse<List<Post>>>
            ) {
                val body = response.body()
                if (response.isSuccessful) {
                    Log.d("PostViewModel", "getVideoPost: ${body?.data}")
                    if (body != null && body.code == 200) {
                        val newPost = body.data?.toMutableList() ?: mutableListOf()
                        bg.launch {
                            dao.insertPosts(newPost)
                            _videoPostList.value = newPost
                            _isLoadMore.value = false
                        }
                    } else {
                        _isLoadMore.value = false
                        _errorMessage.value = body?.message ?: "获取失败"
                        Log.d("PostViewModel", "getVideoPost: ${body?.data}")
                    }
                }
            }
            override fun onFailure(
                call: Call<ApiResponse<List<Post>>>,
                t: Throwable
            ) {
            }
        })
    }
    fun updatePost(context: Context){
        _searchQuery.value = ""
        Log.d("PostViewModel", "updatePost: ${_isNoMore.value}")
        val dao = PostDatabase.getDatabase(context).postDao()
        if(_isRefreshing.value)return
        _isRefreshing.value = true
            val call = ApiClient.apiService.getRandomPosts(RandomPostRequest(null,IMAGE))
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful) {
                        Log.d("PostViewModel", "updatePost: ${body?.data}")
                        if (body != null && body.code == 200) {
                            val newPost = body.data?.toMutableList() ?: mutableListOf()
                            bg.launch {
                                dao.deleteAllPosts()
                                dao.insertPosts(newPost)
                                _postList.value = newPost
                                _isRefreshing.value = false
                            }
                        } else {
                            _isRefreshing.value = false
                            _errorMessage.value = body?.message ?: "获取失败"
                            Log.d("PostViewModel", "updatePost: ${body?.data}")
                        }
                    } else {
                        _isRefreshing.value = false
                        _errorMessage.value =
                            JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }

                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Post>>?>,
                    t: Throwable
                ) {
                    _isRefreshing.value = false
                    _errorMessage.value = "${t.message}"
                }
            })
            _isNoMore.value = false
    }
    private val _isNoMore = MutableStateFlow(false)
    val isNoMore: StateFlow<Boolean> = _isNoMore
    fun loadMorePost(context: Context){
        val dao = PostDatabase.getDatabase(context).postDao()
        if(_isNoMore.value)return
        _isLoadMore.value = true
        bg.launch {
            val list = dao.getAllPostIds()
            val call =
                if(!_isSearching.value)ApiClient.apiService.getRandomPosts(RandomPostRequest(dao.getAllPostIds(),IMAGE))
                else ApiClient.apiService.search(SearchRequest(searchQuery.value, dao.getAllPostIds()))
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    Log.d("PostViewModel", "loadMore: ${list.size}")
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            val newPosts = body.data?.toMutableList() ?: mutableListOf()
                            bg.launch{
                                dao.insertPosts(newPosts)
                            }
                            val merged = _postList.value.toMutableList()
                            merged.addAll(newPosts)
                            _postList.value = merged
                            if(body.message == "NoMore")
                                _isNoMore.value = true
                        } else {
                            _errorMessage.value = body?.message ?: "获取失败"
                        }
                        _isLoadMore.value = false
                        Log.d("PostViewModel", "loadMore: ${_isNoMore.value},message:${body?.message}")
                    }
                    else{
                        _isLoadMore.value = false
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Post>>?>,
                    t: Throwable
                ) {
                    _isLoadMore.value = false
                    _errorMessage.value = "${t.message}"
                }
            })
        }
    }
    fun getMyPosts(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = ApiClient.apiService.getMyPosts("Bearer $token")
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    if (response.isSuccessful) {
                        _myPostList.value = response.body()?.data?.toMutableList() ?: mutableListOf()
                    }
                    else{
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        Log.d("PostViewModel", "获取失败，服务器错误(${response.code()})")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>?>, t: Throwable) {
                    _errorMessage.value = "获取失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "获取失败，服务器错误(${t.message})")
                }
            })
        }
    }
    fun getLikedPost(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = ApiClient.apiService.getMyLikedPosts("Bearer $token")
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    if (response.isSuccessful) {
                        _likedPostList.value = response.body()?.data?.toMutableList() ?: mutableListOf()
                    }
                    else{
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        Log.d("PostViewModel", "获取失败，服务器错误(${response.code()})")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>?>, t: Throwable) {
                    _errorMessage.value = "获取失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "获取失败，服务器错误(${t.message})")
                }
            })
        }
    }
    private val _favouritePostList = MutableStateFlow<MutableList<Post>>(mutableListOf())
    val favouritePostList: StateFlow<List<Post>> = _favouritePostList
    fun getFavouritePost(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = ApiClient.apiService.getFavouritePosts("Bearer $token")
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    if (response.isSuccessful) {
                        _favouritePostList.value = response.body()?.data?.toMutableList() ?: mutableListOf()
                    }
                    else{
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        Log.d("PostViewModel", "获取失败，服务器错误(${response.code()})")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>?>, t: Throwable) {
                    _errorMessage.value = "获取失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "获取失败，服务器错误(${t.message})")
                }
            })
        }
    }

    private val _checkImageList = MutableStateFlow<MutableList<Uri>>(mutableListOf())
    val checkImageList: StateFlow<List<Uri>> = _checkImageList
    fun setCheckImageList(value: List<Uri>) {
        _checkImageList.value = value.toMutableList()
    }
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex
    fun checkImage(uriList: List<Uri>, index:Int){
        _checkImageList.value = uriList.toMutableList()
        _currentIndex.value = index
    }
    private val _ifPost = MutableStateFlow(false)
    val ifPost: StateFlow<Boolean> = _ifPost
    private val _uriList = MutableStateFlow<MutableList<Uri>>(mutableListOf())
    val uriList: StateFlow<List<Uri>> = _uriList
    fun setUriList(value: List<Uri>) {
        value.forEach{
            if(_uriList.value.contains(it) == false){ _uriList.value = (_uriList.value + it).toMutableList() }
        }
    }
    private  val _titleEdit = MutableStateFlow("")
    val titleEdit: StateFlow<String> = _titleEdit
    fun setTitleEdit(value: String) {
        _titleEdit.value = value
    }
    private val _contentEdit = MutableStateFlow("")
    val contentEdit: StateFlow<String> = _contentEdit
    fun setContentEdit(value: String) {
        _contentEdit.value = value
    }
    fun uploadImage(uri: Uri,context: Context, ):String{
        try {
            val stream = context.contentResolver.openInputStream(uri) ?: return ""
            val bytes = stream.readBytes()
            stream.close()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val uploadResp = ApiClient.imgbbService.uploadImage(ApiClient.IMGBB_API_KEY, base64).execute()
            val url = uploadResp.body()?.data?.url
            if (url != null) return url
            else return ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    private val _videoUri = MutableStateFlow<Uri>(Uri.EMPTY)
    val videoUri: StateFlow<Uri> = _videoUri

    fun setVideoUri(uri: Uri) {
        _videoUri.value = uri
        _uriList.value = mutableListOf()
        setPublishType(PublishType.VIDEOPOST)
    }

    private val _publishType = MutableStateFlow(PublishType.IMAGEPOST)
    val publishType : StateFlow<PublishType> = _publishType
    fun setPublishType(value: PublishType) {
        _publishType.value = value
    }

    fun uriToCacheFile(context: Context): File? {
        val uri = _videoUri.value
        return try {
            val name = "video_${System.currentTimeMillis()}.mp4"
            val f = File(context.cacheDir, name)
            val input = context.contentResolver.openInputStream(uri) ?: return null
            input.use { input ->
                f.outputStream().use { input.copyTo(it) }
            }
            f
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // PostViewModel.kt 里


    fun post(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            if (_isLoading.value) return@launch
            val user = DataStorePreference.getUser(context).first()
            val token = DataStorePreference.getToken(context).first()
            if(_isLoading.value){return@launch}
            if (titleEdit.value.isBlank()) {
                _errorMessage.value = "标题不能为空"
                _ifPost.value = false
                return@launch
            }
            if (contentEdit.value.isBlank()) {
                _errorMessage.value = "内容不能为空"
                _ifPost.value = false
                return@launch
            }
            if(_uriList.value.isEmpty() && _videoUri.value == Uri.EMPTY) {
                _errorMessage.value = "请先选择图片或视频"
                _ifPost.value = false
                return@launch
            }
            _isLoading.value = true


                val imageUrls = mutableListOf<String>()
                _uriList.value.forEach { uri ->
                    val url = uploadImage(uri, context)
                    imageUrls.add(url)
                }
                val call = ApiClient.apiService.post(
                    PublishRequest(
                        user.id,
                        titleEdit.value,
                        contentEdit.value,
                        imageUrls
                    ),
                    "Bearer $token"
                )



            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>>,
                    response: Response<ApiResponse<CallBackData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            _isLoading.value = false
                            _ifPost.value = true
                        } else {
                            _ifPost.value = false
                            _errorMessage.value = body?.message ?: "发布失败"
                            _isLoading.value = false
                        }
                    } else {
                        _ifPost.value = false
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        _isLoading.value = false
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                    _errorMessage.value = "网络错误：${t.message}"
                    _isLoading.value = false
                }
            })
        }
    }
    fun postVideo(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            if (_isLoading.value) return@launch
            val token = DataStorePreference.getToken(context).first()
            if(_isLoading.value){return@launch}
            if (titleEdit.value.isBlank()) {
                _errorMessage.value = "标题不能为空"
                _ifPost.value = false
                return@launch
            }
            if (contentEdit.value.isBlank()) {
                _errorMessage.value = "内容不能为空"
                _ifPost.value = false
                return@launch
            }
            _isLoading.value = true

                val uri: Uri = _videoUri.value
                if (uri == Uri.EMPTY) {
                    _errorMessage.value = "请先选择视频"
                    _ifPost.value = false
                    _isLoading.value = false
                    return@launch
                }
                val videoFile = uriToCacheFile(context)
                if (videoFile == null) {
                    _errorMessage.value = "视频文件读取失败，请重新选择"
                    _ifPost.value = false
                    _isLoading.value = false
                    return@launch
                }
                val mime = context.contentResolver.getType(uri) ?: "video/mp4"
                val part = MultipartBody.Part.createFormData(
                    "video",
                    videoFile.name,
                    videoFile.asRequestBody(mime.toMediaTypeOrNull())
                )
                val call = ApiClient.uploadService.postVideo(
                    part,
                    titleEdit.value.toRequestBody("text/plain".toMediaTypeOrNull()),
                    contentEdit.value.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "Bearer $token"
                )


            call.enqueue(object : Callback<ApiResponse<Post>> {
                override fun onResponse(
                    call: Call<ApiResponse<Post>>,
                    response: Response<ApiResponse<Post>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            _isLoading.value = false
                            _ifPost.value = true
                        } else {
                            _ifPost.value = false
                            _errorMessage.value = body?.message ?: "发布失败"
                            _isLoading.value = false
                        }
                    } else {
                        _ifPost.value = false
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        _isLoading.value = false
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Post>>, t: Throwable) {
                    _errorMessage.value = "网络错误：${t.message}"
                    _isLoading.value = false
                }
            })
        }
    }
    fun dissaved(){
        _uriList.value = mutableListOf()
        _titleEdit.value = ""
        _contentEdit.value = ""
    }
    fun resetPublishState(){
        _errorMessage.value = ""
        _ifPost.value = false
    }

    private val _inputContent = MutableStateFlow("")
    val inputContent: StateFlow<String> = _inputContent
    fun setInputContent(value: String) {
        _inputContent.value = value
    }

    private val _isCommentUser = MutableStateFlow(true)
    val isCommentUser: StateFlow<Boolean> = _isCommentUser
    fun setIsCommentUser(value: Boolean) {
        _isCommentUser.value = value
        if (!value) _replyCommentId.value = null
    }
    private val _replyCommentId = MutableStateFlow<String?>(null)

    private val _ifComment = MutableStateFlow(false)
    val ifComment: StateFlow<Boolean> = _ifComment
    fun setIfComment(value: Boolean) {
        _ifComment.value = value
    }
    private val _currentComment = MutableStateFlow(Comment.Companion.empty())
    val currentComment: StateFlow<Comment> = _currentComment
    fun setCurrentComment(value: Comment){
        _currentComment.value = value
    }
    private val _currentReceiver = MutableStateFlow(User.Companion.empty())
    val currentReceiver: StateFlow<User> = _currentReceiver
    fun setCurrentReceiver(value: User){
        _currentReceiver.value = value
    }
    private val _isInputting = MutableStateFlow(false)
    val isInputting: StateFlow<Boolean> = _isInputting
    fun setInputting(value: Boolean) {
        _isInputting.value = value
    }

    fun comment(context : Context){
        var commentType: CommentType = CommentType.POSTCOMMENT
        Log.d("PostViewModel", "isCommentUser: ${_isCommentUser.value}")
        if(_isCommentUser.value) commentType = CommentType.REPLYCOMMENT
        Log.d("PostViewModel", "commentType: $commentType")
        CoroutineScope(Dispatchers.IO).launch {
            if(_isLoading.value) return@launch
            val user = DataStorePreference.getUser(context).first()
            val token = DataStorePreference.getToken(context).first()
            if(_inputContent.value.isBlank()) {
                _errorMessage.value = "评论不能为空"
                return@launch
            }
            _isLoading.value = true
            val call = ApiClient.apiService.comment(
                CommentRequest(
                    senderId = user.id,
                    commentType,
                    receiverId = _currentReceiver.value.id,
                    commentId = _currentComment.value.id,
                    postId = _currentPost.value.id,
                    content = _inputContent.value,
                ),
                "Bearer $token"
            )
            call.enqueue(object : Callback<ApiResponse<CommentCallBack>> {
                override fun onResponse(
                    call: Call<ApiResponse<CommentCallBack>>,
                    response: Response<ApiResponse<CommentCallBack>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful) {
                        _isInputting.value = false
                        _ifComment.value = true
                        _isLoading.value = false
                        _inputContent.value = ""
                        if(commentType == CommentType.POSTCOMMENT){
                            val newComments = (_currentPost.value.comments?.toMutableList() ?: mutableListOf()).apply{
                                add(
                                    Comment(
                                        id = body?.data?.id ?: "",
                                        sender = body?.data?.sender
                                            ?: User.Companion.empty(),
                                        time = body?.data?.time ?: "",
                                        content = body?.data?.content ?: "",
                                        type = CommentType.POSTCOMMENT,
                                        receiver = body?.data?.receiver
                                            ?: User.Companion.empty(),
                                        likes = mutableListOf(),
                                        comments = mutableListOf()
                                    )
                                )
                            }
                            _currentPost.value = _currentPost.value.copy(comments = newComments)
                        }
                        else if (commentType == CommentType.REPLYCOMMENT) {
                            val reply = Comment(
                                id = body?.data?.id ?: "",
                                sender = body?.data?.sender ?: User.Companion.empty(),
                                time = body?.data?.time ?: "",
                                content = body?.data?.content ?: "",
                                type = CommentType.REPLYCOMMENT,
                                receiver = body?.data?.receiver ?: User.Companion.empty(),
                                likes = mutableListOf(),
                                comments = mutableListOf()
                            )
                            val targetId = _currentComment.value.id
                            val updatedComments = (_currentPost.value.comments ?: mutableListOf()).map { c ->
                                if (c.id == targetId) {
                                    c.copy(comments = (c.comments?.toMutableList() ?: mutableListOf()).apply { add(reply) })
                                } else c
                            }
                            _currentPost.value = _currentPost.value.copy(comments = updatedComments.toMutableList())
                        }
                    } else {
                        _isInputting.value = false
                        _ifComment.value = false
                        _isLoading.value = false
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        Log.d("PostViewModel", "评论失败，服务器错误(${response.code()})")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CommentCallBack>>, t: Throwable) {
                    _isInputting.value = false
                    _ifComment.value = false
                    _isLoading.value = false
                    _errorMessage.value = "评论失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "评论失败，服务器错误(${t.message})")
                }
            })

        }
    }
    private val _commentSize = MutableStateFlow(0)
    val commentSize: StateFlow<Int> = _commentSize
    fun defaultCommentSize() {
        _commentSize.value = 0
        _commentSize.value += _currentPost.value.comments?.size ?: 0
        _currentPost.value.comments?.forEach {
            _commentSize.value += it.comments?.size ?: 0
        }
    }


    private val _isCommentLike = MutableStateFlow(false)
    val isCommentLike: StateFlow<Boolean> = _isCommentLike
    fun setIsCommentLike(value: Boolean) {
        _isCommentLike.value = value
    }
    fun like(context: Context, isLiked:Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val user = DataStorePreference.getUser(context).first()
            val call = if(isLiked) ApiClient.apiService.like(
                request = LikeRequest(
                    postId = _currentPost.value.id,
                    commentId = if (_isCommentLike.value) _currentComment.value.id else null,
                    userId = user.id,
                    type = if (_isCommentLike.value) LikeType.COMMENTLIKE else LikeType.POSTLIKE
                )
            ) else ApiClient.apiService.dislike(
                request = LikeRequest(
                    postId = _currentPost.value.id,
                    commentId = if (_isCommentLike.value) _currentComment.value.id else null,
                    userId = user.id,
                    type = if (_isCommentLike.value) LikeType.COMMENTLIKE else LikeType.POSTLIKE
                )
            )
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>?>,
                    response: Response<ApiResponse<CallBackData>?>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            _errorMessage.value = if(isLiked)"点赞成功" else {
                                "取消点赞"
                            }
                        } else {
                            _errorMessage.value = JSONObject(
                                response.errorBody()?.string() ?: ""
                            ).optString("message")
                        }
                    } else {
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(
                    call: Call<ApiResponse<CallBackData>?>,
                    t: Throwable
                ) {
                    _errorMessage.value = "服务器错误(${t.message})"
                    Log.d("PostViewModel", "服务器错误(${t.message})")
                }
            }
            )
        }
    }
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching
    fun setIsSearching(value: Boolean) {
        _isSearching.value = value
    }
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }
    fun search(context: Context){
        val dao = PostDatabase.getDatabase(context).postDao()
        CoroutineScope(Dispatchers.IO).launch {
            val call = ApiClient.apiService.search(
                request = SearchRequest(
                    keyword = _searchQuery.value
                )
            )
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            bg.launch{
                                dao.deleteAllPosts()
                                dao.insertPosts(body.data?.toMutableList() ?: mutableListOf())
                            }
                            _postList.value = mutableListOf()
                            _postList.value = body.data?.toMutableList() ?: mutableListOf()
                            Log.d("PostViewModel", "搜索成功")
                        } else {
                            _errorMessage.value = JSONObject(
                                response.errorBody()?.string() ?: ""
                            ).optString("message")
                        }
                    } else {
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>>, t: Throwable) {
                    Log.d("PostViewModel", "搜索失败，${t.message}")
                }
            })
        }

    }
    fun star(context: Context, ifStar:Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = if (ifStar) ApiClient.apiService.favorite(
                token = "Bearer $token",
                postId = _currentPost.value.id
            ) else ApiClient.apiService.unfavorite(token = "Bearer $token", postId = _currentPost.value.id)
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>?>,
                    response: Response<ApiResponse<CallBackData>?>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            _errorMessage.value = if (ifStar) "收藏成功" else {
                                "取消收藏"
                            }
                        } else {
                            _errorMessage.value = JSONObject(
                                response.errorBody()?.string() ?: ""
                            ).optString("message")
                        }
                    } else {
                        _errorMessage.value =
                            JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<CallBackData>?>,
                    t: Throwable
                ) {
                    _errorMessage.value = "服务器错误(${t.message})"
                    Log.d("PostViewModel", "服务器错误(${t.message})")
                }
            }
            )
        }
    }
    private val _dayHistory = MutableStateFlow(mutableListOf<Post>())
    val dayHistory: StateFlow<List<Post>> = _dayHistory
    private val _yesterdayHistory = MutableStateFlow(mutableListOf<Post>())
    val yesterdayHistory: StateFlow<List<Post>> = _yesterdayHistory
    private val _weekHistory = MutableStateFlow(mutableListOf<Post>())
    val weekHistory: StateFlow<List<Post>> = _weekHistory
    private val _monthHistory = MutableStateFlow(mutableListOf<Post>())
    val monthHistory: StateFlow<List<Post>> = _monthHistory
    fun recordHistory(context: Context,post: Post){
        val dao = PostDatabase.getDatabase(context).postDao()
        val time = System.currentTimeMillis().toString().format("yyyy-MM-dd HH:mm:ss")
        CoroutineScope(Dispatchers.IO).launch {
            val user = DataStorePreference.getUser(context).first()
            val ifExist = dao.getHistoryById(post.id)
            if(ifExist != null){ dao.deleteHistory(ifExist) }
            dao.insertHistory(History(post.id, time, user.id))
            Log.d("PostViewModel", "记录历史成功")
        }
    }
    private val _postType = MutableStateFlow(PostType.LIKE)
    val postType: StateFlow<PostType> = _postType
    fun setPostType(value: PostType) {
        _postType.value = value
    }
    fun getHistory(context: Context){
        val dao = PostDatabase.getDatabase(context).postDao()
        bg.launch {
            val user = DataStorePreference.getUser(context).first()
            val call = ApiClient.apiService.getHistoryPost(
                request = HistoryRequest(
                    postIds= dao.getAllHistoryByUserId(user.id).map { it.id }
                ),
                token = "Bearer ${DataStorePreference.getToken(context).first()}"
            )
            Log.d("PostViewModel", "获取历史记录成功 : ${dao.getAllHistoryByUserId(user.id).map { it.id }}")
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    Log.d("PostViewModel", "获取历史记录成功")
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.code == 200) {
                            bg.launch {
                                if (body.data != null) {
                                    Log.d("PostViewModel", "body.data: ${body.data}")
                                    dao.deleteAllPosts()
                                    dao.insertPosts(body.data.toMutableList())
                                    _dayHistory.value = mutableListOf()
                                    _yesterdayHistory.value = mutableListOf()
                                    _weekHistory.value = mutableListOf()
                                    _monthHistory.value = mutableListOf()
                                    val history = dao.getAllHistory()
                                    val newDayHistory = mutableListOf<Post>()
                                    val newYestodayHistory = mutableListOf<Post>()
                                    val newWeekHistory = mutableListOf<Post>()
                                    val newMonthHistory = mutableListOf<Post>()
                                    history.forEach { history ->
                                        val post = body.data.find { it.id == history.id }
                                        if (post != null){
                                            when (history.getTimeLabel()) {
                                                "今天" -> newDayHistory.add(post)
                                                "昨天" -> newYestodayHistory.add(post)
                                                "一周前" -> newWeekHistory.add(post)
                                                "很久前" -> newMonthHistory.add(post)
                                            }
                                        }
                                    }
                                    Log.d("PostViewModel", "newDayHistory: ${newDayHistory.size}")
                                    _dayHistory.value = newDayHistory
                                    _yesterdayHistory.value = newYestodayHistory
                                    _weekHistory.value = newWeekHistory
                                    _monthHistory.value = newMonthHistory
                                }
                            }
                        } else {
                            Log.d("PostViewModel", "获取历史记录失败")
                            _errorMessage.value = JSONObject(
                                response.errorBody()?.string() ?: ""
                            ).optString("message")
                        }
                    } else {
                        Log.d("PostViewModel", "获取历史记录失败")
                        _errorMessage.value =
                            JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<List<Post>>>,
                    t: Throwable
                ) {
                    _errorMessage.value = "服务器错误(${t.message})"
                    Log.d("PostViewModel", "服务器错误(${t.message})")
                }
            }
            )
        }
    }
    private val _detailUserPost = MutableStateFlow(mutableListOf<Post>())
    val detailUserPost: StateFlow<List<Post>> = _detailUserPost
    fun getUserPost(context: Context, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val call = ApiClient.apiService.getUserPosts(
                token = "Bearer ${DataStorePreference.getToken(context).first()}",
                userId = userId
            )
            call.enqueue(object : Callback<ApiResponse<List<Post>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Post>>>,
                    response: Response<ApiResponse<List<Post>>>
                ) {
                    if (response.isSuccessful) {
                        _detailUserPost.value = response.body()?.data?.toMutableList() ?: mutableListOf()
                    }
                    else{
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>?>, t: Throwable) {
                    _errorMessage.value = "获取失败，服务器错误(${t.message})"
                }
            })
        }
    }
    suspend fun translateText(text: String): String = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.translate(TranslateRequest(text = text)).execute()
            if (response.isSuccessful) {
                if (response.body()?.code == 200) {
                    response.body()?.data?.translation ?: ""
                } else {
                    Log.d("PostViewModel", "翻译失败 code=${response.body()?.code}")
                    _errorMessage.value = response.body()?.message ?: "翻译失败"
                    ""
                }
            } else {
                val msg = response.errorBody()?.string()?.let {
                    JSONObject(it).optString("message")
                } ?: "翻译失败"
                Log.e("PostViewModel", "translate http ${response.code()}: $msg")
                _errorMessage.value = msg
                ""
            }
        } catch (e: Exception) {
            Log.e("PostViewModel", "translate failed: ${e.message}")
            _errorMessage.value = "翻译失败，请检查网络"
            ""
        }
    }
    private val languageIdentifier: LanguageIdentifier = LanguageIdentification.getClient()

    suspend fun detectLanguage(text: String): Boolean {
        return try {
            val languageCode = languageIdentifier.identifyLanguage(text).await()
            languageCode != "und" && languageCode != "zh"
        } catch (e: Exception) {
            false
        }
    }
}