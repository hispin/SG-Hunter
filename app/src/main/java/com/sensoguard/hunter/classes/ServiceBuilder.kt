package com.sensoguard.hunter.classes

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://pushfunction.azurewebsites.net/api/")
        //"https://pushfunction.azurewebsites.net/api/GetTags?code=0S61lCYNRk0vb4rTa9Wqg9SH44wIMszSS1SUjg4vuvwWFafeFPQ1Vg==/") // change this IP for testing by your actual machine IP
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun <T> buildService(service: Class<T>): T {
        return retrofit.create(service)
    }
}