package com.czcz.myapp

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.czcz.myapp.DataStorePreference.saveUserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback

class ViewModel : androidx.lifecycle.ViewModel() {
    private val _account = MutableStateFlow("")
    val account: StateFlow<String> = _account
    fun setAccount(value: String) {
        _account.value = value
    }


    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword
    fun setConfirmPassword(value: String) {
        _confirmPassword.value = value
    }

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
    fun setPassword(value: String) {
        _password.value = value
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading



    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage


    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess


    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess


    private val _autoLogin = MutableStateFlow(false)
    val autoLogin: StateFlow<Boolean> = _autoLogin
    fun setAutoLogin(value: Boolean) {
        _autoLogin.value = value
    }

    private val _uriList = MutableStateFlow< MutableList<Uri>>( mutableListOf())
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

    private val _checkImageList = MutableStateFlow< MutableList<Uri>>( mutableListOf())
    val checkImageList: StateFlow<List<Uri>> = _checkImageList
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _currentPost = MutableStateFlow<Post>(Post.empty())
    val currentPost: StateFlow<Post> = _currentPost
    fun setCurrentPost(post: Post) {
        _currentPost.value = post
    }
    private val _ifPost = MutableStateFlow(false)
    val ifPost: StateFlow<Boolean> = _ifPost

    private val _myPostList = MutableStateFlow< MutableList<Post>>( mutableListOf())
    val myPostList: StateFlow<List<Post>> = _myPostList
    fun addPost(post: Post) {
        _myPostList.value = (_myPostList.value + post).toMutableList()
    }
    fun removePost(post: Post) {
        _myPostList.value = _myPostList.value.filter { it != post }.toMutableList()
    }
    private val _likedPostList = MutableStateFlow< MutableList<Post>>( mutableListOf())
    val likedPostList: StateFlow<List<Post>> = _likedPostList
    fun addLikedPost(post: Post) {
        _likedPostList.value = (_likedPostList.value + post).toMutableList()
    }
    fun removeLikedPost(post: Post) {
        _likedPostList.value = _likedPostList.value.filter { it != post }.toMutableList()
    }

    object ApiClient {
        private const val BASE_URL = "https://bluebook-backend-at73.onrender.com/"
        private const val IMGBB_BASE_URL = "https://api.imgbb.com/1/"
        const val IMGBB_API_KEY = "d35841f781c7eb9c8bd4f0e6f6d00b6a"

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val imgbbRetrofit: Retrofit = Retrofit.Builder()
            .baseUrl(IMGBB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
        val imgbbService: ImgBBService = imgbbRetrofit.create(ImgBBService::class.java)
    }

    fun register() {
        if (account.value.isBlank()) {
            _errorMessage.value = "账号不能为空"
            return
        }
        if (password.value.isBlank()) {
            _errorMessage.value = "密码不能为空"
            return
        }
        if (password.value != confirmPassword.value) {
            _errorMessage.value = "两次输入的密码不一致"
            return
        }

        _errorMessage.value = ""
        _isLoading.value = true
        val call = ApiClient.apiService.register(RegisterRequest(account.value, password.value))
        call.enqueue(object : Callback<ApiResponse<CallBackData>> {
            override fun onResponse(
                call: Call<ApiResponse<CallBackData>>,
                response: Response<ApiResponse<CallBackData>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        _registerSuccess.value = true
                    } else {
                        _errorMessage.value = body?.message ?: "注册失败"
                    }
                } else {
                    _errorMessage.value = "注册失败，服务器错误(${response.code()})"
                }
            }

            override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
            }
        })
    }

    fun login(context: Context) {
        if (account.value.isBlank()) {
            _errorMessage.value = "账号不能为空"
            return
        }
        if (password.value.isBlank()) {
            _errorMessage.value = "密码不能为空"
            return
        }
        _errorMessage.value = ""
        _isLoading.value = true
        val call = ApiClient.apiService.login(LoginRequest(account.value, password.value))

        call.enqueue(object : Callback<ApiResponse<CallBackData>> {
            override fun onResponse(
                call: Call<ApiResponse<CallBackData>>,
                response: Response<ApiResponse<CallBackData>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        _loginSuccess.value = true
                        if(body.data != null && body.data.user != null)CoroutineScope(Dispatchers.IO).launch{ saveUserInfo(body.data.token ?: "", context, _autoLogin.value, data = body.data.user) }
                        else {
                            Toast.makeText(context, "用户信息读取失败", Toast.LENGTH_SHORT).show()}
                    } else {
                        _errorMessage.value = body?.message ?: "登录失败"
                    }
                } else {
                    _errorMessage.value = "登录失败，服务器错误(${response.code()})"
                }
            }

            override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
            }
        })
    }
    fun autoLogin(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val savedAutoLogin = DataStorePreference.getAutoLogin(context).first()
            if(savedAutoLogin) {
                _isLoading.value = true
                val account = DataStorePreference.getAccount(context).first()
                val password = DataStorePreference.getPassword(context).first()
                setAccount(account)
                setPassword(password)
                setAutoLogin(true)
                val token = DataStorePreference.getToken(context).first()
                val call = ApiClient.apiService.autologin(AutoLoginRequest(account, token))
                call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                    override fun onResponse(
                        call: Call<ApiResponse<CallBackData>>,
                        response: Response<ApiResponse<CallBackData>>
                    ) {
                        _isLoading.value = false
                        if (response.isSuccessful) {

                            val body = response.body()
                            if (body != null && body.code == 200) {
                                _loginSuccess.value = true
                            } else {
                                _errorMessage.value = body?.message ?: "登录失败"
                            }
                        } else {
                            _errorMessage.value = "登录失败，服务器错误(${response.code()})"
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                        _isLoading.value = false
                        _errorMessage.value = "网络错误：${t.message}"
                    }
                })
            }
        }
    }

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
            _isLoading.value = true

            val imageUrls = mutableListOf<String>()
            for (uri in _uriList.value) {
                try {
                    val stream = context.contentResolver.openInputStream(uri) ?: continue
                    val bytes = stream.readBytes()
                    stream.close()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    val uploadResp = ApiClient.imgbbService.uploadImage(ApiClient.IMGBB_API_KEY, base64).execute()
                    val url = uploadResp.body()?.data?.url
                    if (url != null) imageUrls.add(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val call = ApiClient.apiService.post(PublishRequest(user.id, titleEdit.value, contentEdit.value, imageUrls), "Bearer $token")

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
                        _errorMessage.value = "发布失败，服务器错误(${response.code()})"
                        _isLoading.value = false
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                    Toast.makeText(context, "网络错误：${t.message}", Toast.LENGTH_SHORT).show()
                    _isLoading.value = false
                }
            })
        }
    }

    private val _postList = MutableStateFlow< MutableList<Post>>( mutableListOf())
    val postList: StateFlow<List<Post>> = _postList

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun updatePost(){
        _isRefreshing.value = true
        _postList.value = mutableListOf()

        val call = ApiClient.apiService.getRandomPosts()
        call.enqueue(object : Callback<ApiResponse<List<Post>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Post>>>,
                response: Response<ApiResponse<List<Post>>>
            ) {
                _isRefreshing.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.code == 200) {
                        body.data?.forEach {
                            if(!_postList.value.contains(it)){ _postList.value = (_postList.value + it).toMutableList() }
                        }
                    } else {
                        _errorMessage.value = body?.message ?: "获取失败"
                    }
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<Post>>?>,
                t: Throwable
            ) {
                _isRefreshing.value = false
                _errorMessage.value = "获取失败，服务器错误(${t.message})"
            }
        })
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
                        _errorMessage.value = "获取失败，服务器错误(${response.code()})"
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Post>>?>, t: Throwable) {
                    _errorMessage.value = "获取失败，服务器错误(${t.message})"
                }
            })
        }
    }


    fun quitLogin(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            DataStorePreference.clearData(context)
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

    fun checkImage(uriList: List<Uri>,index:Int){
        _checkImageList.value = uriList.toMutableList()
        _currentIndex.value = index
    }

}
