package com.sensoguard.hunter.services

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.sensoguard.hunter.global.AMAZON_PRECESS_DIALOG_VALUE
import com.sensoguard.hunter.global.AMAZON_PRECESS_TYPE_KEY
import com.sensoguard.hunter.global.AMAZON_PRECESS_WITH_USER_VALUE
import com.sensoguard.hunter.global.TOKEN_AMAZON_KEY_PREF
import com.sensoguard.hunter.global.UserSession
import com.sensoguard.hunter.global.getStringInPreference
import com.sensoguard.hunter.global.requestLoginAmazon
import com.sensoguard.hunter.global.setStringInPreference

class LoginAmazonIntentWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    var FCM_token: String? = null
    var amazonProcessType: String? = null
    override fun doWork(): Result {

        //get Input Data back using "inputData" variable
        amazonProcessType = inputData.getString(AMAZON_PRECESS_TYPE_KEY)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (task.isSuccessful && task.result != null) {
                    FCM_token = task.result
                    val currentToken = getStringInPreference(context, TOKEN_AMAZON_KEY_PREF, "")

                    if (amazonProcessType.equals(AMAZON_PRECESS_DIALOG_VALUE)) {

                        // if the process come from dialog then send request immediately
                        //send request
                        setStringInPreference(context, TOKEN_AMAZON_KEY_PREF, FCM_token)
                        requestLoginAmazon(
                            context,
                            UserSession.instance.getUserAmazon()?.email,
                            UserSession.instance.getUserAmazon()?.password,
                            FCM_token
                        )

                    } else if (amazonProcessType.equals(AMAZON_PRECESS_WITH_USER_VALUE)) {
                        if (!currentToken.equals(FCM_token)) {
                            setStringInPreference(context, TOKEN_AMAZON_KEY_PREF, FCM_token)
                            requestLoginAmazon(
                                context,
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

        return Result.success()
    }
}