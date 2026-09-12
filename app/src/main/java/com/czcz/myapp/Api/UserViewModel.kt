package com.czcz.myapp.Api
import android.content.Context
import android.util.Log
import com.czcz.myapp.Api.Models.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.czcz.myapp.Api.PostViewModel.ApiClient
import com.czcz.myapp.DataStorePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback


class UserViewModel:ViewModel() {
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage
    fun resetErrorMessage() {
        _errorMessage.value = ""
    }


    fun followUser(context: Context,followdId: String, ifFollowed: Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            val token = DataStorePreference.getToken(context).first()
            val call = if(ifFollowed){ ApiClient.apiService.follow(followdId, "Bearer $token") } else { ApiClient.apiService.unfollow(followdId, "Bearer $token") }
            Log.d("UserViewModel", "followUser: ${call.request().url}")
            call.enqueue(object : Callback<ApiResponse<CallBackData>> {
                override fun onResponse(
                    call: Call<ApiResponse<CallBackData>>,
                    response: Response<ApiResponse<CallBackData>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if(body != null && body.code == 200){
                            if(ifFollowed){ _errorMessage.value = "关注成功" }
                            if(!ifFollowed){ _errorMessage.value = "取消关注成功" }
                        }
                        else{
                            _errorMessage.value = body?.message ?: "获取失败"
                        }
                    } else {
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<CallBackData>>, t: Throwable) {
                }
            })

        }
    }
}