package com.sensoguard.hunter.classes;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sensoguard.hunter.activities.InitAppActivity;
import com.sensoguard.hunter.activities.MyScreensActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public MyExceptionHandler(Activity a) {
        activity = a;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        String errorMsg = printLog(ex);

        //restart the app
        Intent intent = new Intent(activity, InitAppActivity.class);
        //you can use this String to know what caused the exception and in which Activity
        intent.putExtra("stacktrace", errorMsg);
        activity.startActivity(intent);

        activity.finish();
        System.exit(2);


//        if (BuildConfig.REPORT_CRASH) {
//
//            FirebaseCrash.report(ex);
//
//            restartApp();
//        } else if (ex.getStackTrace() != null) {
//
//            sendEmail(errorMsg);
//            activity.finish();
//            System.exit(2);
//        }
        //throw new RuntimeException("This is a crash");
    }

    private void sendEmail(String msg) {

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hag.swead@gmail.com","tomer@sensoguard.com" });
        i.putExtra(Intent.EXTRA_SUBJECT, "SensoGuard app has been crashed");
        i.putExtra(Intent.EXTRA_TEXT, msg);
        try {
            activity.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }

    }

    private void restartApp(String msgError) {
        Intent intent = new Intent(activity, MyScreensActivity.class);
        //intent.putExtra("crash", true);
        intent.putExtra("stacktrace", msgError);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager mgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        activity.finish();
        System.exit(2);
    }

    private String printLog(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(new PrintWriter(sw));
        if (pw != null) {
            Log.e("MyExceptionHandler", Log.getStackTraceString(ex));
        }
        return Log.getStackTraceString(ex);
    }
}

