package com.example.mystoryapp.retrofit

import com.example.mystoryapp.response.GetStoriesResponse
import com.example.mystoryapp.response.ImageUploadResponse
import com.example.mystoryapp.response.LoginResponse
import com.example.mystoryapp.response.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @GET("stories")
    fun getStories(
        @Header("Authorization") token: String
    ): Call<GetStoriesResponse>

    @FormUrlEncoded
    @POST("register")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun postUserLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("stories")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Part file:MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Call<ImageUploadResponse>
}