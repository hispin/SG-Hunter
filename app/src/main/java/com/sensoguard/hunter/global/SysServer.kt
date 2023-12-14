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


    UserSession.instance.setInstanceUserAzure(
        name = user,
        pw = pw
    )

    UserSession.instance.getUserAzure()?.let { user ->
        apiService.addUser(user) {

            if (it != null) {

                val result = it.get("result")

                if (result.asString == AZURA_POST_RESULT_OK) {
                    tags = it.getAsJsonArray("tags")
                    if (tags != null && tags?.size()!! > 0 && UserSession.instance.getUserAzure() != null) {
                        //UserSession.instance.setTags(tags!!)
                        //val test = UserSession.instance.getTags()
                        wContext.get()?.let { it1 -> storeTagsToLocally(tags!!, it1) }
                        wContext.get()
                            ?.let { it1 ->
                                storeUserAzureToLocally(
                                    UserSession.instance.getUserAzure()!!,
                                    it1, USER_INFO_AZURE_KEY
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


