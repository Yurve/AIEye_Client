package com.example.AIEye.retrofit

import com.google.gson.annotations.SerializedName

data class Login(
    @field:SerializedName("email") val email: String,
    @field:SerializedName("password") val password: String,
    @field:SerializedName("fcmToken") val token: String,
    @field:SerializedName("fcmTokenName") val fcmTokenName: String
)