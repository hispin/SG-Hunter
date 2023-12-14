//package com.sensoguard.hunter.services
//
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.google.android.gms.tasks.Task
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.gson.JsonArray
//import com.sensoguard.hunter.classes.AuthResult
//import com.sensoguard.hunter.classes.RestApiService
//import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_FAILED
//import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_SUCCESS
//import com.sensoguard.hunter.global.AMAZON_PRECESS_DIALOG_VALUE
//import com.sensoguard.hunter.global.AMAZON_PRECESS_TYPE_KEY
//import com.sensoguard.hunter.global.AMAZON_PRECESS_WITH_USER_VALUE
//import com.sensoguard.hunter.global.AZURA_POST_RESULT_ERROR_NO_DATA
//import com.sensoguard.hunter.global.AZURA_POST_RESULT_OK
//import com.sensoguard.hunter.global.TOKEN_AMAZON_KEY_PREF
//import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
//import com.sensoguard.hunter.global.USER_INFO_AZURE_KEY
//import com.sensoguard.hunter.global.UserSession
//import com.sensoguard.hunter.global.getStringInPreference
////import com.sensoguard.hunter.global.requestLoginAmazon
//import com.sensoguard.hunter.global.setStringInPreference
//import com.sensoguard.hunter.global.storeTagsToLocally
//import com.sensoguard.hunter.global.storeUserAmazonToLocally
//import com.sensoguard.hunter.global.storeUserAzureToLocally
//import java.lang.ref.WeakReference
//
//class LoginAmazonIntentWorker(val context: Context, workerParams: WorkerParameters) :
//    Worker(context, workerParams) {
//
//    var response: LiveData<Int> = MutableLiveData()
//
//    var FCM_token: String? = null
//    var amazonProcessType: String? = null
//    override fun doWork(): Result {
//
//        //get Input Data back using "inputData" variable
//        amazonProcessType = inputData.getString(AMAZON_PRECESS_TYPE_KEY)
//
//
//
//        FirebaseMessaging.getInstance().token
//            .addOnCompleteListener { task: Task<String> ->
//                if (task.isSuccessful && task.result != null) {
//                    FCM_token = task.result
//                    val currentToken = getStringInPreference(context, TOKEN_AMAZON_KEY_PREF, "")
//
//                    if (amazonProcessType.equals(AMAZON_PRECESS_DIALOG_VALUE)) {
//                        Log.d("testToken", FCM_token + "")
//                        // if the process come from dialog then send request immediately
//                        //send request
//                        setStringInPreference(context, TOKEN_AMAZON_KEY_PREF, FCM_token)
//                        requestLoginAmazon(
//                            UserSession.instance.getUserAmazon()?.email,
//                            UserSession.instance.getUserAmazon()?.password,
//                            FCM_token
//                        )
//                    } else if (amazonProcessType.equals(AMAZON_PRECESS_WITH_USER_VALUE)) {
//                        if (!currentToken.equals(FCM_token)) {
//                            setStringInPreference(context, TOKEN_AMAZON_KEY_PREF, FCM_token)
//                            requestLoginAmazon(
//                                UserSession.instance.getUserAmazon()?.email,
//                                UserSession.instance.getUserAmazon()?.password,
//                                FCM_token
//                            )
//                        } else {
//                            // DO NOTHING
//                        }
//                    }
//                } else {
//                    //TODO send ERROR message
//                }
//            }
//
//        return Result.success()
//    }
//
//    ////////////////////////////////
//    /**
//     *check if the user is exist and get it's tags
//     */
//    fun checkUserGetTags(context: Context, user: String, pw: String): JsonArray? {
//        val wContext =
//            WeakReference<Context>(context)
//        var tags: JsonArray? = null
//        val apiService = RestApiService()
//
//
//        UserSession.instance.setInstanceUserAzure(
//            name = user,
//            pw = pw
//        )
//
//        UserSession.instance.getUserAzure()?.let { user ->
//            apiService.addUser(user) {
//
//                if (it != null) {
//
//                    val result = it.get("result")
//
//                    if (result.asString == AZURA_POST_RESULT_OK) {
//                        tags = it.getAsJsonArray("tags")
//                        if (tags != null && tags?.size()!! > 0 && UserSession.instance.getUserAzure() != null) {
//                            //UserSession.instance.setTags(tags!!)
//                            //val test = UserSession.instance.getTags()
//                            wContext.get()?.let { it1 -> storeTagsToLocally(tags!!, it1) }
//                            wContext.get()
//                                ?.let { it1 ->
//                                    storeUserAzureToLocally(
//                                        UserSession.instance.getUserAzure()!!,
//                                        it1, USER_INFO_AZURE_KEY
//                                    )
//                                }
//                        }
//
//                    }
//                    context.sendBroadcast(Intent(result.asString))
//
//                    Log.d("retrofit", "accept user id")
//                    // it = newly added user parsed as response
//                    // it?.id = newly added user ID
//                } else {
//                    context.sendBroadcast(Intent(AZURA_POST_RESULT_ERROR_NO_DATA))
//                    //Timber.d("Error registering new user")
//                }
//
//            }
//        }
//        return tags
//    }
//
//    /**
//     *send request of login amazon
//     */
//    fun requestLoginAmazon(user: String?, pw: String?, token: String?) {
//        val apiService = RestApiService()
//
//
//        if (user != null && pw != null && token != null) {
//            UserSession.instance.setInstanceUserAmazon(
//                name = user,
//                pw = pw,
//                token
//            )
//        }
//
//        UserSession.instance.getUserAmazon()?.let { user ->
//            apiService.loginAmazon(user) {
//
//                if (it != null) {
//
//                    val result: AuthResult = it
//                    if (result.success == true) {
//                        if (UserSession.instance.getUserAmazon() != null) {
//                            context
//                                .let { it1 ->
//                                    storeUserAmazonToLocally(
//                                        UserSession.instance.getUserAmazon()!!,
//                                        it1, USER_INFO_AMAZON_KEY
//                                    )
//                                }
//                        }
//                        applicationContext.sendBroadcast(Intent(AMAZONE_POST_LOGIN_RESULT_SUCCESS))
//                    } else {
//                        context.sendBroadcast(Intent(AMAZONE_POST_LOGIN_RESULT_FAILED))
//                    }
//
//                    Log.d("retrofit", "accept user id")
//                } else {
//                    context.sendBroadcast(Intent(AMAZONE_POST_LOGIN_RESULT_FAILED))
//                }
//
//            }
//        }
//    }
//    ////////////////////////////////
//
//}