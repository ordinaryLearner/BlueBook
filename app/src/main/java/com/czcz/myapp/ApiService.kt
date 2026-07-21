package com.czcz.myapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("health")
    fun health(): Call<HealthResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<RegisterData>>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<RegisterData>>
}
