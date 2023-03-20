package com.sensoguard.hunter.classes

import android.util.Log
import com.google.gson.JsonObject
import com.sensoguard.hunter.interfaces.RestApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*dddddd*/
/**
 *
 *
 *
 */
class RestApiService {
    fun addUser(userData: UserInfo, onResult: (JsonObject?) -> Unit) {
        val retrofit = ServiceBuilder.buildService(RestApi::class.java)
        retrofit.addUser(userData).enqueue(
            object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    Log.d("", t.toString())
                    onResult(null)
                }

                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    val tags = response.body()
                    onResult(tags)
                }
//                override fun onFailure(call: Call<UserInfo>, t: Throwable) {
//                    onResult(null)
//                }
//
//                override fun onResponse(call: Call<UserInfo>, response: Response<UserInfo>) {
//                    val addedUser = response.body()
//                    onResult(addedUser)
//                }
            }
        )
    }
}