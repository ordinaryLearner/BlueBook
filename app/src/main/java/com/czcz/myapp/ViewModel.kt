package com.czcz.myapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback

class ViewModel {
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

    fun setAccount(value: String) {
        _account.value = value
    }
    fun setConfirmPassword(value: String) {
        _confirmPassword.value = value
    }
    fun setPassword(value: String) {
        _password.value = value
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
        call.enqueue(object : Callback<ApiResponse<RegisterData>> {
            override fun onResponse(
                call: Call<ApiResponse<RegisterData>>,
                response: Response<ApiResponse<RegisterData>>
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

            override fun onFailure(call: Call<ApiResponse<RegisterData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
            }
        })
    }

    fun login() {
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
        call.enqueue(object : Callback<ApiResponse<RegisterData>> {
            override fun onResponse(
                call: Call<ApiResponse<RegisterData>>,
                response: Response<ApiResponse<RegisterData>>
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

            override fun onFailure(call: Call<ApiResponse<RegisterData>>, t: Throwable) {
                _isLoading.value = false
                _errorMessage.value = "网络错误：${t.message}"
            }
        })
    }
}
