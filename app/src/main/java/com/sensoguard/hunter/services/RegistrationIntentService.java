package com.sensoguard.hunter.services;

import static com.sensoguard.hunter.global.ConstsKt.REGISTER_ID_KEY;
import static com.sensoguard.hunter.global.ConstsKt.SHARED_PREF_FILE_NAME;
import static com.sensoguard.hunter.global.SysServerKt.checkUserGetTags;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.sensoguard.hunter.global.NotificationSettings;
import com.sensoguard.hunter.global.UserSession;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    SharedPreferences sharedPreferences;
    String resultString = null;
    String regID = null;
    String storedToken = null;
    String FCM_token = null;

    private NotificationHub hub;
    private String myTag;


    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);// PreferenceManager.getDefaultSharedPreferences(this);
        resultString = null;
        regID = null;
        storedToken = null;

        String actionType = Objects.requireNonNull(intent.getExtras()).getString("actionType", null);
        String user = null;
        String pw = null;
        if (UserSession.Companion.getInstance().getUserAzure() != null) {
            user = Objects.requireNonNull(UserSession.Companion.getInstance().getUserAzure()).getName();
            pw = UserSession.Companion.getInstance().getUserAzure().getPw();
        }

        if (actionType != null && actionType.equals("post")) {
            if (user != null && pw != null)
                checkUserGetTags(this, user, pw);
        } else if (actionType != null && actionType.equals("register")) {
            //myTag = UserSession.Companion.getInstance().getTags();//Objects.requireNonNull(intent.getExtras()).getString("myTag",null);
            ArrayList<String> tags = UserSession.Companion.getInstance().getTags();
            if (tags != null && tags.size() > 0) {
                myTag = tags.get(0);
                for (int i = 1; i < tags.size(); i++) {
                    myTag = myTag + "," + tags.get(i);
                }
                register();
            }
        }

    }


    private void register() {
        try {

            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FCM_token = task.getResult();
                            Intent inn = new Intent("get.token.notification");
                            inn.putExtra("token", FCM_token);
                            sendBroadcast(inn);
                            Log.d(TAG, "FCM Registration Token: " + FCM_token);
                        }
                    });


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

            TimeUnit.SECONDS.sleep(1);

            // Storing the registration ID that indicates whether the generated token has been
            // sent to your server. If it is not stored, send the token to your server.
            // Otherwise, your server should have already received the token.
            if (((regID = sharedPreferences.getString(REGISTER_ID_KEY, null)) == null)) {

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.d(TAG, "Attempting a new registration with NH using FCM token : " + FCM_token);
                //regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                regID = hub.register(FCM_token, myTag).getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString(REGISTER_ID_KEY, regID).apply();
                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
            }

            // Check to see if the token has been compromised and needs refreshing.
            else if (!(storedToken = sharedPreferences.getString("FCMtoken", "")).equals(FCM_token)) {

                NotificationHub hub = new NotificationHub(NotificationSettings.HubName,
                        NotificationSettings.HubListenConnectionString, this);
                Log.d(TAG, "NH Registration refreshing with token : " + FCM_token);
                //regID = hub.register(FCM_token).getRegistrationId();

                // If you want to use tags...
                // Refer to : https://azure.microsoft.com/documentation/articles/notification-hubs-routing-tag-expressions/
                // regID = hub.register(token, "tag1,tag2").getRegistrationId();
                regID = hub.register(FCM_token, myTag).getRegistrationId();

                resultString = "New NH Registration Successfully - RegId : " + regID;
                Log.d(TAG, resultString);

                sharedPreferences.edit().putString(REGISTER_ID_KEY, regID).apply();
                sharedPreferences.edit().putString("FCMtoken", FCM_token).apply();
            } else {
                resultString = "Previously Registered Successfully - RegId : " + regID;
            }

            //sendEmail(FCM_token);
            Intent inn = new Intent("get.token.notification");
            inn.putExtra("token", FCM_token);
            sendBroadcast(inn);

        } catch (Exception e) {
            Log.e(TAG, resultString = "Failed to complete registration", e);
            // If an exception happens while fetching the new token or updating registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
        }

        // Notify UI that registration has completed.
//        if (MainActivity.isVisible) {
//            MainActivity.mainActivity.ToastNotify(resultString);
//        }
    }

    private void sendEmail(String msg) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hag.swead@gmail.com", "tomer@sensoguard.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try {
            getApplicationContext().startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }

    }
}
