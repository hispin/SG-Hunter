package com.sensoguard.hunter.services

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.microsoft.windowsazure.messaging.NotificationHub
import com.sensoguard.hunter.global.NotificationSettings
import com.sensoguard.hunter.global.REGISTER_ID_KEY
import com.sensoguard.hunter.global.SHARED_PREF_FILE_NAME
import com.sensoguard.hunter.global.UserSession.Companion.instance
import com.sensoguard.hunter.global.checkUserGetTags
import java.util.*
import java.util.concurrent.TimeUnit

class RegistrationWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val TAG = "RegIntentService"
    var sharedPreferences: SharedPreferences? = null
    var resultString: String? = null
    var regID: String? = null
    var storedToken: String? = null
    var FCM_token: String? = null

    private val hub: NotificationHub? = null
    private var myTag: String? = null
    override fun doWork(): Result {
        sharedPreferences = context.getSharedPreferences(
            SHARED_PREF_FILE_NAME,
            Context.MODE_PRIVATE
        ) // PreferenceManager.getDefaultSharedPreferences(this);

        resultString = null
        regID = null
        storedToken = null

        val actionType = inputData.getString("actionType")
//        val actionType =
//            Objects.requireNonNull<Bundle>(intent.getExtras()).getString("actionType", null)
        var user: String? = null
        var pw: String? = null
        if (instance.getUser() != null) {
            user = Objects.requireNonNull(instance.getUser())?.name
            pw = instance.getUser()!!.pw
        }

        if (actionType != null && actionType == "post") {
            if (user != null && pw != null) checkUserGetTags(context, user, pw)
        } else if (actionType != null && actionType == "register") {
            //myTag = UserSession.Companion.getInstance().getTags();//Objects.requireNonNull(intent.getExtras()).getString("myTag",null);
            val tags = instance.getTags()
            if (tags != null && tags.size > 0) {
                myTag = tags[0]
                for (i in 1 until tags.size) {
                    myTag = myTag + "," + tags[i]
                }
                register()
            }
        }
        return Result.success()
    }

    private fun register() {
        try {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task: Task<String> ->
                    if (task.isSuccessful && task.result != null) {
                        FCM_token = task.result
                        val inn = Intent("get.token.notification")
                        inn.putExtra("token", FCM_token)
                        context.sendBroadcast(inn)
                        Log.d(
                            TAG,
                            "FCM Registration Token: $FCM_token"
                        )
                    }
                }


//            FirebaseMessaging.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
//                @Override
//                public void onSuccess(InstanceIdResult instanceIdResult) {
//                    FCM_token = instanceIdResult.getToken();
//                    Intent inn = new Intent("get.token.notification");
//                    inn.putExtra("token", FCM_token);
//                    sendBroadcast(inn);
//                    Log.d(TAG, "FCM Registration Token: " + FCM_token);
//                }
//            });
            TimeUnit.SECONDS.sleep(1)

            // Storing the registration ID that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server.
            // Otherwise, your server should have already received the token.
            if (sharedPreferences!!.getString(REGISTER_ID_KEY, null).also { regID = it } == null) {
                val hub = NotificationHub(
                    NotificationSettings.HubName,
                    NotificationSettings.HubListenConnectionString, context
                )
                Log.d(
                    TAG,
                    "Attempting a new registration with NH using FCM token : $FCM_token"
                )
                //regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                regID = hub.register(FCM_token, myTag).registrationId
                resultString = "New NH Registration Successfully - RegId : $regID"
                Log.d(TAG, resultString!!)
                sharedPreferences!!.edit().putString(REGISTER_ID_KEY, regID).apply()
                sharedPreferences!!.edit().putString("FCMtoken", FCM_token).apply()
            } else if (sharedPreferences!!.getString("FCMtoken", "")
                    .also { storedToken = it } != FCM_token
            ) {
                val hub = NotificationHub(
                    NotificationSettings.HubName,
                    NotificationSettings.HubListenConnectionString, context
                )
                Log.d(
                    TAG,
                    "NH Registration refreshing with token : $FCM_token"
                )
                //regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();
                regID = hub.register(FCM_token, myTag).registrationId
                resultString = "New NH Registration Successfully - RegId : $regID"
                Log.d(TAG, resultString!!)
                sharedPreferences!!.edit().putString(REGISTER_ID_KEY, regID).apply()
                sharedPreferences!!.edit().putString("FCMtoken", FCM_token).apply()
            } else {
                resultString = "Previously Registered Successfully - RegId : $regID"
            }

            //sendEmail(FCM_token);
            val inn = Intent("get.token.notification")
            inn.putExtra("token", FCM_token)
            context.sendBroadcast(inn)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete registration".also {
                resultString = it
            }, e)
            // If an exception happens while fetching the new token or updating registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
//        if (MainActivity.isVisible) {
//            MainActivity.mainActivity.ToastNotify(resultString);
//        }
    }

    private fun sendEmail(msg: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("hag.swead@gmail.com", "tomer@sensoguard.com"))
        i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed")
        i.putExtra(Intent.EXTRA_TEXT, msg)
        try {
            applicationContext.startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            ex.printStackTrace()
        }
    }

}