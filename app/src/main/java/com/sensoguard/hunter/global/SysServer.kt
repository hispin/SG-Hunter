package com.sensoguard.hunter.global

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.JsonArray
import com.sensoguard.hunter.classes.RestApiService
import java.lang.ref.WeakReference


/**
 *check if the user is exist and get it's tags
 */
fun checkUserGetTags(context: Context, user: String, pw: String): JsonArray? {
    val wContext =
        WeakReference<Context>(context)
    var tags: JsonArray? = null
    val apiService = RestApiService()


    UserSession.instance.setInstanceUser(
        name = user,
        pw = pw
    )
//    UserSession.instance.setInstanceUser(name = "test1@test.com",
//        pw = "1234")
//    val userInfo = UserInfo(  name = "test1@test.com",
//        pw = "1234" )
//    val userInfo = UserInfo(  name = "testOK",
//        pw = "1234" )

//    val userInfo = UserInfo(  userId = null,
//        userName = "Alex",
//        userEmail = "alex@gmail.com",
//        userAge = "32",
//        userUid = "164E92FC-D37A-4946-81CB-29DE7EE4B124" )

    UserSession.instance.getUser()?.let { user ->
        apiService.addUser(user) {

            if (it != null) {

                val result = it.get("result")

                if (result.asString == AZURA_POST_RESULT_OK) {
                    tags = it.getAsJsonArray("tags")
                    if (tags != null && tags?.size()!! > 0 && UserSession.instance.getUser() != null) {
                        //UserSession.instance.setTags(tags!!)
                        //val test = UserSession.instance.getTags()
                        wContext.get()?.let { it1 -> storeTagsToLocally(tags!!, it1) }
                        wContext.get()
                            ?.let { it1 ->
                                storeUserToLocally(
                                    UserSession.instance.getUser()!!,
                                    it1
                                )
                            }
                    }

                }
                context.sendBroadcast(Intent(result.asString))

                Log.d("retrofit", "accept user id")
                // it = newly added user parsed as response
                // it?.id = newly added user ID
            } else {
                context.sendBroadcast(Intent(AZURA_POST_RESULT_ERROR_NO_DATA))
                //Timber.d("Error registering new user")
            }

        }
    }
    return tags
}