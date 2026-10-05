package com.sensoguard.hunter.interfaces

import com.google.gson.JsonObject
import com.sensoguard.hunter.BuildConfig
import com.sensoguard.hunter.classes.AuthResult
import com.sensoguard.hunter.classes.UserInfoAmazon
import com.sensoguard.hunter.classes.UserInfoAzure
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RestApi {
    @Headers("Content-Type: application/json")
    @POST("GetTags?code=" + BuildConfig.AZURE_FUNCTION_CODE)
    fun addUser(@Body userData: UserInfoAzure): Call<JsonObject>


    @Headers("Content-Type: application/json")
    @POST("https://outwatch.sensoguard.com/api/auth/login")
    fun loginAmazon(@Body userData: UserInfoAmazon): Call<AuthResult>
}