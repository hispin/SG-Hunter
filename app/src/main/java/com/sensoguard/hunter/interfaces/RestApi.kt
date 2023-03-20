package com.sensoguard.hunter.interfaces

import com.google.gson.JsonObject
import com.sensoguard.hunter.classes.UserInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RestApi {
    @Headers("Content-Type: application/json")
    @POST("GetTags?code=0S61lCYNRk0vb4rTa9Wqg9SH44wIMszSS1SUjg4vuvwWFafeFPQ1Vg==")
    fun addUser(@Body userData: UserInfo): Call<JsonObject>
}