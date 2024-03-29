package com.sensoguard.hunter.classes

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import com.sensoguard.hunter.global.getAlarmsFromLocally
import com.sensoguard.hunter.global.storeAlarmsToLocally
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class Alarm {


    var isReadyToDelete: Boolean = false
    var id: String? = null
    var name: String? = null
    var type: String? = null
    private var currentDate: String? = null
    var isArmed: Boolean? = null
    var timeInMillis: Long? = null


    var longitude: Double?=null
    var latitude: Double?=null
    var isLocallyDefined: Boolean = false
    var isActive: Boolean? = false
    var imgsPath: String? = null
    var myBitmap: Bitmap? = null
    var isCameFromEmail = false
    var isLoadPhoto = false
    var msgNumber: Int? = null
    var batteryVal: Float? = null
    var wifiVal: Float? = null
    var fromEmail: String? = null
//    var zuraSentDate: Long? = null
//    var acceptDate: Long? = null
//    var fileDateSent: Long?=null
//    var startPushSent: Long?=null

    constructor()


    constructor(
        id: String?,
        name: String?,
        type: String?,
        currentDate: String,
        isArmed: Boolean?,
        timeInMillis: Long?
    ) {

        this.id = id
        this.name = name
        this.type = type
        this.currentDate = currentDate
        this.isArmed = isArmed
        this.timeInMillis = timeInMillis
    }

    constructor(
        id: String?,
        name: String?,
        type: String?,
        timeInMillis: Long?,
        imgsPath: String?,
        latitude: Double?,
        longitude: Double?,
        wifiVal: Float?,
        batteryVal: Float
    ) {
        this.id = id
        this.name = name
        this.type = type
        this.isArmed = true
        this.timeInMillis = timeInMillis
        this.imgsPath = imgsPath
        this.latitude = latitude
        this.longitude = longitude
        this.wifiVal = wifiVal
        this.batteryVal = batteryVal
    }

    //add active alarm to history
    fun addAlarmToHistory(context: Context) {
        //use WeakReference if the activity is no longer alive
        val wContext: WeakReference<Context> =
            WeakReference(context)
        val alarms = wContext.get()?.let { getAlarmsFromLocally(it) }
        alarms?.add(this)
        if (wContext.get() != null) {
            alarms?.let { storeAlarmsToLocally(it, wContext.get()!!) }
        }
    }

//    //add active alarm to history
//    fun addAlarmToHistory(
//        currentSensorLocally: Camera,
//        type: String,
//        context: Context,
//        myCalendar: Calendar
//    ) {
//        //use WeakReference if the activity is no longer alive
//        val wContext: WeakReference<Context> =
//            WeakReference(context)
//        val tmp = Calendar.getInstance()
//        val resources = wContext.get()?.resources
//        val locale =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources?.configuration?.locales?.getFirstMatch(
//                resources.assets?.locales
//            )
//            else resources?.configuration?.locale
//        val dateFormat = SimpleDateFormat("kk:mm dd/MM/yy", locale)
//        val dateString = dateFormat.format(tmp.time)
//
//        //val alarm= Alarm(
//        this.id = currentSensorLocally.getId()
//        this.name = currentSensorLocally.getName()
//        this.type = type
//        this.currentDate = dateString
//        this.isArmed = currentSensorLocally.isArmed()
//        this.timeInMillis = tmp.timeInMillis
//        //)
//        this.latitude = currentSensorLocally.getLatitude()
//        this.longitude = currentSensorLocally.getLongtitude()
//
//        this.isLocallyDefined = true
//
//
//        val alarms = wContext.get()?.let { populateAlarmsFromLocally(it) }
//        alarms?.add(this)
//        if (wContext.get() != null) {
//            alarms?.let { storeAlarmsToLocally(it, wContext.get()!!) }
//        }
//    }

    fun getCurrentDate(): String {
        return currentDate.toString()
    }

    //add active alarm to history
    fun addAlarmToHistory(
        currentSensorLocally: Camera,
        type: String,
        context: Context,
        tmp: Calendar,
        batteryVal: Float,
        wifiVal: Float
    ) {
        //use WeakReference if the activity is no longer alive
        val wContext: WeakReference<Context> =
            WeakReference(context)
        //val tmp = Calendar.getInstance()
        val resources = wContext.get()?.resources
        val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) resources?.configuration?.locales?.getFirstMatch(
                resources.assets?.locales
            )
            else resources?.configuration?.locale
        val dateFormat = SimpleDateFormat("kk:mm dd/MM/yyyy", locale)
        val dateString = dateFormat.format(tmp.time)

        //val alarm= Alarm(
        this.id = currentSensorLocally.getId()
        this.name = currentSensorLocally.getName()
        this.type = type
        this.currentDate = dateString
        this.isArmed = currentSensorLocally.isArmed()
        this.timeInMillis = tmp.timeInMillis
        //)
        this.latitude = currentSensorLocally.getLatitude()
        this.longitude = currentSensorLocally.getLongtitude()
        this.batteryVal = batteryVal
        this.wifiVal = wifiVal

        this.isLocallyDefined = true
        //this.imgsPath=imagePath

        val alarms = wContext.get()?.let { getAlarmsFromLocally(it) }
        alarms?.add(this)
        if (wContext.get() != null) {
            alarms?.let { storeAlarmsToLocally(it, wContext.get()!!) }
        }
    }

    //update the image
    fun updateAlarm(imgPath: String, context: Context) {
        //use WeakReference if the activity is no longer alive
        val wContext: WeakReference<Context> =
            WeakReference(context)
        val alarms = wContext.get()?.let { getAlarmsFromLocally(it) }

        val iteratorList = alarms?.listIterator()

        while (iteratorList != null && iteratorList.hasNext()) {
            val item = iteratorList.next()
            if (item.id == this.id && this.msgNumber == item.msgNumber) {
                item.isLoadPhoto = false
                item.imgsPath = imgPath
            }
        }

        if (wContext.get() != null) {
            alarms?.let { storeAlarmsToLocally(it, wContext.get()!!) }
        }


    }




}