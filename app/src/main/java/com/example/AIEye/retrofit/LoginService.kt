package com.example.AIEye.retrofit

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers("Content-Type: application/json")
    @POST("api/Account/SignIn")
    fun sendLogin(@Body login: Login): Call<LoginResponse>
}