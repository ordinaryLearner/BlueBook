package com.czcz.myapp

import android.content.Context
import android.net.Uri
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
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
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
    private val _uriList = MutableStateFlow< MutableList<Uri>>( mutableListOf())
    val uriList: StateFlow<List<Uri>> = _uriList
    private val _checkImageList = MutableStateFlow< MutableList<Uri>>( mutableListOf())
    val checkImageList: StateFlow<List<Uri>> = _checkImageList
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    fun setAccount(value: String) {
        _account.value = value
    }
    fun setConfirmPassword(value: String) {
        _confirmPassword.value = value
    }
    fun setPassword(value: String) {
        _password.value = value
    }
    fun setUriList(value: List<Uri>) {
        value.forEach{
            if(_uriList.value.contains(it) == false){ _uriList.value = (_uriList.value + it).toMutableList() }
        }
    }
    fun setAutoLogin(value: Boolean) {
        _autoLogin.value = value
    }

    object ApiClient {
        private const val BASE_URL = "https://bluebook-backend-at73.onrender.com/"

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
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


    fun quitLogin(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            DataStorePreference.clearData(context)
         }
    }
    fun dissaved(){
        _uriList.value = mutableListOf()
    }

    fun checkImage(uriList: List<Uri>,index:Int){
        _checkImageList.value = uriList.toMutableList()
        _currentIndex.value = index
    }

}
