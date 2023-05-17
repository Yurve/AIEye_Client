package com.example.AIEye.retrofit

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @field:SerializedName("isSucceeded") val isSucceeded: Boolean,
    @field:SerializedName("errors") val errors: List<String>
)