package com.czcz.myapp.Api

import android.content.Context
import android.util.Log
import android.widget.Toast
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

class LoginViewModel : ViewModel() {

    private val _account = MutableStateFlow("")
    val account: StateFlow<String> = _account
    fun setAccount(value: String) {
        _account.value = value
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading



    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage


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

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess


    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess


    private val _autoLogin = MutableStateFlow(false)
    val autoLogin: StateFlow<Boolean> = _autoLogin
    fun setAutoLogin(value: Boolean) {
        _autoLogin.value = value
    }
    fun quitLogin(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            DataStorePreference.clearData(context)
        }
        _loginSuccess.value = false
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
        val call = PostViewModel.ApiClient.apiService.register(
            Models.RegisterRequest(
                account.value,
                password.value
            )
        )
        call.enqueue(object : Callback<Models.ApiResponse<Models.CallBackData>> {
            override fun onResponse(
                call: Call<Models.ApiResponse<Models.CallBackData>>,
                response: Response<Models.ApiResponse<Models.CallBackData>>
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
                    _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                }
            }

            override fun onFailure(call: Call<Models.ApiResponse<Models.CallBackData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
                Log.d("LoginViewModel", t.message ?: "Network error")
            }
        })
    }
    private val _followersList = MutableStateFlow<MutableList<String>>(mutableListOf())
    val followersList: StateFlow<List<String>> = _followersList
    fun deleteFollower(userId: String) {
        _followersList.value.remove(userId)
    }
    fun addFollower(userId: String) {
        _followersList.value.add(userId)
    }

    private val _fansList = MutableStateFlow<MutableList<String>>(mutableListOf())
    val fansList: StateFlow<List<String>> = _fansList

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
        val call = com.czcz.myapp.Api.PostViewModel.ApiClient.apiService.login(
            Models.LoginRequest(
                account.value,
                password.value
            )
        )

        call.enqueue(object : Callback<Models.ApiResponse<Models.CallBackData>> {
            override fun onResponse(
                call: Call<Models.ApiResponse<Models.CallBackData>>,
                response: Response<Models.ApiResponse<Models.CallBackData>>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("LoginViewModel", "Successful")
                    Log.d("LoginViewModel", body?.data?.user?.username.orEmpty())
                    if (body != null && body.code == 200) {
                        _loginSuccess.value = true
                        if(body.data != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                DataStorePreference.saveUserInfo(
                                    body.data.token ?: "",
                                    context,
                                    _autoLogin.value,
                                    data = body.data.user
                                )
                                _fansList.value = body.data.user.fans ?: mutableListOf()
                                _followersList.value = body.data.user.followers ?: mutableListOf()
                                Log.d("LoginViewModel", "fans = ${_fansList.value}")
                                Log.d("LoginViewModel", "followers = ${_followersList.value}")
                            }
                        }
                        else {
                            Toast.makeText(context, "用户信息读取失败", Toast.LENGTH_SHORT).show()}
                    } else {
                        _errorMessage.value = body?.message ?: "登录失败"
                    }
                } else {
                    Log.d("LoginViewModel", "")
                    _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                }
            }

            override fun onFailure(call: Call<Models.ApiResponse<Models.CallBackData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
                Log.d("LoginViewModel", t.message ?: "Network error")
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
                val call = PostViewModel.ApiClient.apiService.autologin(
                    Models.AutoLoginRequest(
                        account,
                        token
                    )
                )
                call.enqueue(object : Callback<Models.ApiResponse<Models.CallBackData>> {
                    override fun onResponse(
                        call: Call<Models.ApiResponse<Models.CallBackData>>,
                        response: Response<Models.ApiResponse<Models.CallBackData>>
                    ) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.code == 200) {
                                Log.d("LoginViewModel", body.data?.user.toString())
                                if(body.data != null) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        DataStorePreference.saveUserInfo(
                                            body.data.token ?: "",
                                            context,
                                            _autoLogin.value,
                                            data = body.data.user
                                        )
                                        _loginSuccess.value = true
                                        _fansList.value = body.data.user.fans ?: mutableListOf()
                                        _followersList.value = body.data.user.followers ?: mutableListOf()
                                        Log.d("LoginViewModel", "user jointime: ${body.data.user.joinTime}")
                                        Log.d("LoginViewModel", "fans = ${_fansList.value}")
                                        Log.d("LoginViewModel", "followers = ${_followersList.value}")
                                    }
                                }
                            } else {
                                _loginSuccess.value = false
                                _errorMessage.value = body?.message ?: "登录失败"
                            }
                        } else {
                            _loginSuccess.value = false
                            _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                        }
                    }

                    override fun onFailure(call: Call<Models.ApiResponse<Models.CallBackData>>, t: Throwable) {
                        _loginSuccess.value = false
                        _isLoading.value = false
                        _errorMessage.value = "网络错误：${t.message}"
                    }
                })
            }
        }
    }
}