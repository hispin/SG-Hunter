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
    fun addUser(userData: UserInfoAzure, onResult: (JsonObject?) -> Unit) {
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
            }
        )
    }

    //send login amazon request
    fun loginAmazon(userData: UserInfoAmazon, onResult: (AuthResult?) -> Unit) {
        val retrofit = ServiceBuilder.buildService(RestApi::class.java)
        retrofit.loginAmazon(userData).enqueue(
            object : Callback<AuthResult> {
                override fun onFailure(call: Call<AuthResult>, t: Throwable) {
                    Log.d("", t.toString())
                    onResult(null)
                }

                override fun onResponse(call: Call<AuthResult>, response: Response<AuthResult>) {
                    val tags = response.body()
                    onResult(tags)
                }
            }
        )
    }


}