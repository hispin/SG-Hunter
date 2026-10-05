package com.sensoguard.hunter.services

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.microsoft.windowsazure.messaging.NotificationHub
import com.sensoguard.hunter.global.NotificationSettings
import com.sensoguard.hunter.global.REGISTER_ID_KEY
import com.sensoguard.hunter.global.SHARED_PREF_FILE_NAME
import java.util.concurrent.TimeUnit

/**
 * registers the FCM token with the user's tags in the Azure notification hub
 * (replaces RegistrationIntentService)
 */
class HubRegistrationWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val tags = inputData.getString(KEY_TAGS)
        if (tags.isNullOrEmpty()) {
            return Result.success()
        }

        return try {
            val fcmToken = Tasks.await(FirebaseMessaging.getInstance().token, 30, TimeUnit.SECONDS)
            val prefs = applicationContext.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
            val regId = prefs.getString(REGISTER_ID_KEY, null)
            val storedToken = prefs.getString(FCM_TOKEN_PREF, "")

            // register when not registered yet, or when the token was refreshed
            if (regId == null || storedToken != fcmToken) {
                val hub = NotificationHub(
                    NotificationSettings.HubName,
                    NotificationSettings.HubListenConnectionString,
                    applicationContext
                )
                val newRegId = hub.register(fcmToken, tags).registrationId
                Log.d(TAG, "New NH Registration Successfully - RegId : $newRegId")
                prefs.edit()
                    .putString(REGISTER_ID_KEY, newRegId)
                    .putString(FCM_TOKEN_PREF, fcmToken)
                    .apply()
            } else {
                Log.d(TAG, "Previously Registered Successfully - RegId : $regId")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete registration", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HubRegistrationWorker"
        private const val KEY_TAGS = "tags"
        private const val FCM_TOKEN_PREF = "FCMtoken"
        private const val WORK_NAME = "hub_registration"

        fun enqueue(context: Context, tags: List<String>) {
            val request = OneTimeWorkRequestBuilder<HubRegistrationWorker>()
                .setInputData(workDataOf(KEY_TAGS to tags.joinToString(",")))
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }
    }
}
