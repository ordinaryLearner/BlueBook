package com.czcz.myapp.Api

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
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
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.Api.PostViewModel.ApiClient
import androidx.core.net.toUri

class InfoViewModel: ViewModel() {
    private val _currentBackground = MutableStateFlow<Uri>(Uri.EMPTY)
    val currentBackground: StateFlow<Uri> = _currentBackground
    private val _currentAvatar = MutableStateFlow<Uri>(Uri.EMPTY)
    val currentAvatar: StateFlow<Uri> = _currentAvatar

    private val _fansList = MutableStateFlow<List<String>>(emptyList())
    val fansList: StateFlow<List<String>> = _fansList
    fun setFansList(value: List<String>) {
        _fansList.value = value
    }
    private val _followers = MutableStateFlow<List<String>>(emptyList())
    val followers: StateFlow<List<String>> = _followers
    fun setFollowers(value: List<String>) {
        _followers.value = value
    }
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
    fun resetErrorMessage() {
        _errorMessage.value = ""
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess
    fun resetIsSuccess() {
        _isSuccess.value = false
    }

    private val _newUsername = MutableStateFlow("")
    val newUsername: StateFlow<String> = _newUsername
    fun setNewUsername(value: String) {
        _newUsername.value = value
    }

    private val _newBio = MutableStateFlow("")
    val newBio: StateFlow<String> = _newBio
    fun setNewBio(value: String) {
        _newBio.value = value
    }
    private val _newAvatar = MutableStateFlow<Uri>(Uri.EMPTY)
    val newAvatar: StateFlow<Uri> = _newAvatar
    fun setNewAvatar(value: Uri) {
        _newAvatar.value = value
    }
    private val _newBackground = MutableStateFlow<Uri>(Uri.EMPTY)
    fun setNewBackground(value: Uri) {
        _newBackground.value = value
    }
    private val _onlyUpdateAvatar = MutableStateFlow(false)
    fun setOnlyUpdateAvatar(value: Boolean) {
        _onlyUpdateAvatar.value = value
    }
    private val _onlyUpdateBackground = MutableStateFlow(false)
    fun setOnlyUpdateBackground(value: Boolean) {
        _onlyUpdateBackground.value = value
    }
    private val _newAvatarUrl = MutableStateFlow("")
    private val _newBackgroundUrl = MutableStateFlow("")
    private val _isUpdateProfile = MutableStateFlow(false)
    val isUpdateProfile: StateFlow<Boolean> = _isUpdateProfile
    fun setIsUpdateProfile(value: Boolean) {
        _isUpdateProfile.value = value
    }

        fun uploadImage(image: Uri, context: Context): String {
        try {
            val stream = context.contentResolver.openInputStream(image) ?: return ""
            val bytes = stream.readBytes()
            stream.close()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val uploadResp =
                ApiClient.imgbbService.uploadImage(ApiClient.IMGBB_API_KEY, base64).execute()
            return uploadResp.body()?.data?.url ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
    fun getMe(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = ApiClient.apiService.getMe(GetMeRequest(token))
            Log.d("PostViewModel", "getMe: $token")
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>>,
                    response: Response<ApiResponse<CallBackData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        CoroutineScope(Dispatchers.Main).launch {
                            if (body != null && body.code == 200) {
                                if (body.data != null) {
                                    if(body.data.user.background != null){ _currentBackground.value = body.data.user.background.toUri() }
                                    if(body.data.user.avatar != null){ _currentAvatar.value = body.data.user.avatar.toUri() }
                                }
                            }
                            else{
                                _errorMessage.value = "更新失败，服务器错误(${body?.message ?: ""})"
                            }
                        }
                    } else {
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        Log.d("PostViewModel", "getMe更新失败，服务器错误(${response.errorBody()?.string() ?: ""})")
                    }
                }
                override fun onFailure(
                    call: Call<ApiResponse<CallBackData>>,
                    t: Throwable
                ) {
                    _errorMessage.value = "更新失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "更新失败，服务器错误(${t.message})")
                }
            })
        }
    }
    fun updateProfile(context: Context){
        if(_newUsername.value.isBlank() && !_onlyUpdateAvatar.value && !_onlyUpdateBackground.value){
            _errorMessage.value = "请输入用户名"
            return
        }
        _isLoading.value = true
        if(_newAvatar.value != Uri.EMPTY) _newAvatarUrl.value = uploadImage(_newAvatar.value, context)
        if(_newBackground.value != Uri.EMPTY) _newBackgroundUrl.value = uploadImage(_newBackground.value, context)

        CoroutineScope(Dispatchers.IO).launch {
            val user = DataStorePreference.getUser(context).first()
            val token = DataStorePreference.getToken(context).first()
            val autoLogin = DataStorePreference.getAutoLogin(context).first()
            val call = ApiClient.apiService.updateProfile(
                token = "Bearer $token",
                request = UpdateProfileRequest(
                    username = _newUsername.value.ifBlank { user.username },
                    bio = _newBio.value.ifBlank { user.bio },
                    avatar = _newAvatarUrl.value.ifBlank { user.avatar },
                    background = _newBackgroundUrl.value.ifBlank { user.background }
                )
            )
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>>,
                    response: Response<ApiResponse<CallBackData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        _errorMessage.value = "更新成功"
                        CoroutineScope(Dispatchers.Main).launch {
                            if (body != null && body.code == 200) {
                                if (body.data != null) {
                                    DataStorePreference.saveUserInfo(
                                        body.data.token ?: "",
                                        context,
                                        autoLogin,
                                        data = body.data.user
                                    )
                                    if(body.data.user.background != null){ _currentBackground.value = body.data.user.background.toUri() }
                                }
                            }
                        }
                        _isSuccess.value = true
                    } else {
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(
                    call: Call<ApiResponse<CallBackData>>,
                    t: Throwable
                ) {
                    _errorMessage.value = "更新失败，服务器错误(${t.message})"
                    Log.d("PostViewModel", "更新失败，服务器错误(${t.message})")
                }
            })
            _isLoading.value = false
            _newAvatar.value = Uri.EMPTY
            _newBackground.value = Uri.EMPTY
            _onlyUpdateAvatar.value = false
            _onlyUpdateBackground.value = false
            _isUpdateProfile.value = false
        }
    }
    private val _detailUser = MutableStateFlow<User>(User.empty())
    val detailUser: StateFlow<User> = _detailUser
    fun getDetailUser(context: Context,userId:String) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            val call = ApiClient.apiService.getUser(token = "Bearer ${DataStorePreference.getToken(context).first()}", id = userId)
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>>,
                    response: Response<ApiResponse<CallBackData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("InfoViewModel", "获取用户信息成功，user: ${body?.data?.user}")
                        if (body != null && body.code == 200) {
                            _detailUser.value = User(
                                id = body.data?.user?.id ?: "",
                                username = body.data?.user?.username ?: "",
                                account = body.data?.user?.account ?: "",
                                avatar = body.data?.user?.avatar ?: "",
                                background = body.data?.user?.background ?: "",
                                bio = body.data?.user?.bio ?: "",
                                joinTime = body.data?.user?.joinTime ?: "",
                                followers = body.data?.user?.followers?.toMutableList() ?: mutableListOf(),
                                fans = body.data?.user?.fans?.toMutableList() ?: mutableListOf(),
                                likes = body.data?.user?.likes ?: 0
                            )
                            if(body.data?.user?.background != null){ _currentBackground.value = body.data.user.background.toUri() }
                        }
                    }
                }
                override fun onFailure(
                    call: Call<ApiResponse<CallBackData>>,
                    t: Throwable
                ) {
                    _errorMessage.value = "获取用户信息失败，服务器错误(${t.message})"
                    Log.d("InfoViewModel", "获取用户信息失败，服务器错误(${t.message})")
                }
            })
            _isLoading.value = false
        }
    }
}