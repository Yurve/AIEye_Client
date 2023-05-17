package com.example.AIEye.retrofit

import com.google.gson.annotations.SerializedName

data class LoginResponse (
    @field:SerializedName("IsSucceeded") val isSucceeded : Boolean,
    @field:SerializedName("ErrorMessage") val errorMessage: String,
    @field:SerializedName("Token") val token: String,
    @field:SerializedName("UserDTO") val userDTO: UserDTO
    )


data class UserDTO(
    @field:SerializedName("Id") val id : String,
    @field:SerializedName("Name") val name:String,
    @field:SerializedName("Email") val email:String
)