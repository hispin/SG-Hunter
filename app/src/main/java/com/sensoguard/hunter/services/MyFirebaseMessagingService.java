package com.sensoguard.hunter.services;

import static com.sensoguard.hunter.global.ConstsKt.CURRENT_ITEM_TOP_MENU_KEY;
import static com.sensoguard.hunter.global.ConstsKt.DETECT_ALARM_KEY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sensoguard.hunter.activities.MyScreensActivity;
import com.sensoguard.hunter.classes.AlarmParsing;

import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "nh-demo-channel-id";
    public static final String NOTIFICATION_CHANNEL_NAME = "Notification Hubs Demo Channel";
    public static final String NOTIFICATION_CHANNEL_DESCRIPTION = "Notification Hubs Demo Channel";
    public static final int NOTIFICATION_ID = 1;
    static Context ctx;
    NotificationCompat.Builder builder;
    private final String TAG = "FirebaseService";
    private NotificationManager mNotificationManager;

    public static void createChannelAndHandleNotifications(Context context) {
        ctx = context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(NOTIFICATION_CHANNEL_DESCRIPTION);
            channel.setShowBadge(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //nhMessage = remoteMessage.getData().values().iterator().next();
        Intent myIntent = remoteMessage.toIntent();

        if (myIntent != null) {

//                int[] arr= new int[1];
//                arr[1]=5;

            //parse extra intent to alarm and add it to locally
            AlarmParsing.getInstance().parsePushToAlarm(myIntent).addAlarmToHistory(this);

            Intent inn = new Intent(DETECT_ALARM_KEY);
            //myIntent.setAction(DETECT_ALARM_KEY);
            sendBroadcast(inn);

            //start media
            startServiceMedia();

            createChannelAndHandleNotifications(getApplicationContext());
            sendNotification(myIntent);
        }
    }

    private void sendNotification(Intent myIntent) {

        if (myIntent == null)
            return;

        Intent intent = new Intent(ctx, MyScreensActivity.class);
        intent.putExtra(CURRENT_ITEM_TOP_MENU_KEY, 2);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = Objects.requireNonNull(myIntent.getExtras()).getString("title");
        String message = Objects.requireNonNull(myIntent.getExtras()).getString("message");

        //add extras data that accepted from push
        intent.putExtras(myIntent);

        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);


        long oneTimeID = SystemClock.uptimeMillis();

        PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            //set different request code to make different extra for each notification
            contentIntent = PendingIntent.getActivity(ctx, (int) oneTimeID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                ctx,
                NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //remove after click on notification
                .setAutoCancel(true);

        notificationBuilder.setContentIntent(contentIntent);

        //send
        mNotificationManager.notify((int) oneTimeID, notificationBuilder.build());

    }

    //start service media
    private void startServiceMedia() {
        Intent serviceIntent = new Intent(this, MediaService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}
