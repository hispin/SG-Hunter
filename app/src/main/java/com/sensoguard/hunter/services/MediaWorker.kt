package com.sensoguard.hunter.services

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
import com.sensoguard.hunter.global.ALARM_FLICKERING_DURATION_KEY
import com.sensoguard.hunter.global.IS_NOTIFICATION_SOUND_KEY
import com.sensoguard.hunter.global.IS_VIBRATE_WHEN_ALARM_KEY
import com.sensoguard.hunter.global.SELECTED_NOTIFICATION_SOUND_KEY
import com.sensoguard.hunter.global.getBooleanInPreference
import com.sensoguard.hunter.global.getLongInPreference
import com.sensoguard.hunter.global.getStringInPreference
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class MediaWorker (val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private var scheduleTaskExecutor: ScheduledExecutorService? = null

    override fun doWork(): Result {
        playVibrate()
        if (playAlarmSound()) {
            shutDownTimer()
            startTimer()
        }
        return Result.success()
    }

    //execute vibrate
    private fun playVibrate(): Boolean {
        val isNotificationVibration =
            getBooleanInPreference(context, IS_VIBRATE_WHEN_ALARM_KEY, true)
        if (!isNotificationVibration) {
            return false
        }

        val VIBRO_TIME=2000L

        var vibrator:Vibrator?=null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator

        }else{
            vibrator=context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes: AudioAttributes=
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM).build()
                val ve=VibrationEffect.createOneShot(
                    VIBRO_TIME, VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator.vibrate(ve, audioAttributes)
            } else {
                vibrator.vibrate(VIBRO_TIME)
            }
        }
        return true
    }
    private var rington: Ringtone? = null

    //stop play the sound of alarm
    private fun stopPlayingAlarm() {
        if (rington != null && rington?.isPlaying!!) {
            rington?.stop()
        }
    }

    //execute vibrate
    private fun playAlarmSound(): Boolean {

        val isNotificationSound =
            getBooleanInPreference(context, IS_NOTIFICATION_SOUND_KEY, true)
        if (!isNotificationSound) {
            return false
        }

        val selectedSound =
            getStringInPreference(context, SELECTED_NOTIFICATION_SOUND_KEY, "-1")

        if (!selectedSound.equals("-1")) {

            try {
                val uri = Uri.parse(selectedSound)
                if (rington != null && rington!!.isPlaying) {
                    //if the sound it is already played,
                    rington?.stop()

                    Handler(Looper.getMainLooper()).postDelayed({
                        rington = RingtoneManager.getRingtone(context, uri)
                        rington?.play()
                    }, 1000)

//                    Handler().postDelayed({
//                        rington = RingtoneManager.getRingtone(context, uri)
//                        rington?.play()
//                    }, 1000)
                } else {
                    rington = RingtoneManager.getRingtone(context, uri)
                    rington?.play()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return true
    }

    private fun startTimer() {
        //if there is already at least one alarm ,it is not necessary to initial the timer
        if (scheduleTaskExecutor == null
            || (scheduleTaskExecutor?.isShutdown != null && scheduleTaskExecutor?.isShutdown!!)
        ) {
            scheduleTaskExecutor = Executors.newScheduledThreadPool(1)
            executeTimer()
        }
    }

    // execute the time
    private fun executeTimer() {

        val timeout = getLongInPreference(
            context,
            ALARM_FLICKERING_DURATION_KEY,
            ALARM_FLICKERING_DURATION_DEFAULT_VALUE_SECONDS
        )
        // This schedule a task to run every 1 second:
        scheduleTaskExecutor?.scheduleAtFixedRate({
            stopPlayingAlarm()
            shutDownTimer()
            //stopSelf()

        }, timeout, timeout, TimeUnit.SECONDS)

    }
    //shut down the timer
    private fun shutDownTimer() {
        try {
            scheduleTaskExecutor?.shutdownNow()
        } catch (ex: Exception) {
        } finally {

        }
    }

    override fun onStopped() {
        super.onStopped()
        stopPlayingAlarm()
        shutDownTimer()
    }

}