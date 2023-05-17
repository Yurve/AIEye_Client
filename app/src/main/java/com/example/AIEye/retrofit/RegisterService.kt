package com.example.AIEye.retrofit

import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body

interface RegisterService {
    @Headers("Content-Type: application/json")
    @POST("api/Account/SignUp")
    fun sendRegister(@Body register: Register): Call<RegisterResponse>
}