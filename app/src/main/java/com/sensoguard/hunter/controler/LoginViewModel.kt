package com.sensoguard.hunter.controler

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.sensoguard.hunter.classes.AuthResult
import com.sensoguard.hunter.classes.RestApiService
import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_FAILED
import com.sensoguard.hunter.global.AMAZONE_POST_LOGIN_RESULT_SUCCESS
import com.sensoguard.hunter.global.AMAZON_PRECESS_DIALOG_VALUE
import com.sensoguard.hunter.global.AMAZON_PRECESS_WITH_USER_VALUE
import com.sensoguard.hunter.global.TOKEN_AMAZON_KEY_PREF
import com.sensoguard.hunter.global.USER_INFO_AMAZON_KEY
import com.sensoguard.hunter.global.UserSession
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.setStringInPreference
import com.sensoguard.hunter.global.storeUserAmazonResultToLocally

/**
 * amazon login
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var response: MutableLiveData<String>? = null

    var FCM_token: String? = null

    init {
        response = MutableLiveData(AMAZONE_POST_LOGIN_RESULT_FAILED)
    }

    fun requestLoginAmazon(amazonProcessType: String, isAllAlarmsProcess: Boolean) {
        //get Input Data back using "inputData" variable

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (task.isSuccessful && task.result != null) {
                    FCM_token = task.result
                    val currentToken = getStringInPreference(
                        getApplication<Application>().applicationContext,
                        TOKEN_AMAZON_KEY_PREF,
                        ""
                    )

                    if(isAllAlarmsProcess) {
                        Log.d("testToken", FCM_token + "")
                        // if the process come from dialog then send request immediately
                        //send request
                        setStringInPreference(
                            getApplication<Application>().applicationContext,
                            TOKEN_AMAZON_KEY_PREF,
                            FCM_token
                        )
                        requestLoginAmazon(
                            UserSession.instance.getUserAmazonResult()?.email,
                            UserSession.instance.getUserAmazonResult()?.password,
                            FCM_token
                        )
                    }else if (amazonProcessType.equals(AMAZON_PRECESS_DIALOG_VALUE) ) {
                        Log.d("testToken", FCM_token + "")
                        // if the process come from dialog then send request immediately
                        //send request
                        setStringInPreference(
                            getApplication<Application>().applicationContext,
                            TOKEN_AMAZON_KEY_PREF,
                            FCM_token
                        )
                        requestLoginAmazon(
                            UserSession.instance.getUserAmazon()?.email,
                            UserSession.instance.getUserAmazon()?.password,
                            FCM_token
                        )
                    } else if (amazonProcessType.equals(AMAZON_PRECESS_WITH_USER_VALUE)) {
                        if (!currentToken.equals(FCM_token)) {
                            setStringInPreference(
                                getApplication<Application>().applicationContext,
                                TOKEN_AMAZON_KEY_PREF,
                                FCM_token
                            )
                            requestLoginAmazon(
                                UserSession.instance.getUserAmazon()?.email,
                                UserSession.instance.getUserAmazon()?.password,
                                FCM_token
                            )
                        } else {
                            // DO NOTHING
                        }
                    }
                } else {
                    //TODO send ERROR message
                }
            }

    }

    /**
     *send request of login amazon
     */
    fun requestLoginAmazon(user: String?, pw: String?, token: String?) {
        val apiService = RestApiService()


        if (user != null && pw != null && token != null) {
            UserSession.instance.setInstanceUserAmazon(
                name = user,
                pw = pw,
                token
            )
        }

        UserSession.instance.getUserAmazon()?.let { user ->
            apiService.loginAmazon(user) {

                if (it != null) {

                    val result: AuthResult = it

                    if (result.success == true) {
                        if (UserSession.instance.getUserAmazon() != null) {
                            val email:String?=UserSession.instance.getUserAmazon()!!.email
                            val pass=UserSession.instance.getUserAmazon()!!.password
                            val fcm_token=UserSession.instance.getUserAmazon()!!.token_fcm
                            UserSession.instance.setInstanceUserAmazonResult(
                                email,pass,
                                fcm_token,result.token,result.imagesBaseUrl)
                            getApplication<Application>().applicationContext
                                .let { it1 ->
                                    storeUserAmazonResultToLocally(
                                        UserSession.instance.getUserAmazonResult()!!,
                                        it1, USER_INFO_AMAZON_KEY
                                    )
                                }
                        }
                        response?.value = AMAZONE_POST_LOGIN_RESULT_SUCCESS
                    } else {
                        response?.value = AMAZONE_POST_LOGIN_RESULT_FAILED
                    }

                    Log.d("retrofit", "accept user id")
                } else {
                    response?.value = AMAZONE_POST_LOGIN_RESULT_FAILED
                }

            }
        }
    }

}