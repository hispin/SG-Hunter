package com.sensoguard.hunter.global

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.MyEmailAccount
import com.sensoguard.hunter.classes.UserInfoAmazonResult
import com.sensoguard.hunter.classes.UserInfoAzure
import java.lang.ref.WeakReference

// inspection fix: removed unused function

//store the sensors to locally
fun storeSensorsToLocally(sensors: ArrayList<Camera>, context: Context) {

    var detectorsJsonStr:String?=""
    if(sensors!=null && sensors.isNotEmpty()){ // inspection fix: isNotEmpty()
        detectorsJsonStr= convertToGson(sensors)
    }
    setStringInPreference(context,DETECTORS_LIST_KEY_PREF,detectorsJsonStr)
}

//get the sensors from locally
fun getSensorsFromLocally(activity: Context): ArrayList<Camera>? {
    val sensors: ArrayList<Camera>?
    val detectorListStr=getStringInPreference(activity,DETECTORS_LIST_KEY_PREF, ERROR_RESP)

    sensors = if(detectorListStr.equals(ERROR_RESP)){
        ArrayList()
    }else {
        detectorListStr?.let { convertJsonToSensorList(it) }
    }
    return sensors
}

//get the alarms from locally
fun getAlarmsFromLocally(context: Context): java.util.ArrayList<Alarm>? {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val alarms: java.util.ArrayList<Alarm>?
    val alarmListStr = getStringInPreference(wContext.get(), ALARM_LIST_KEY_PREF, ERROR_RESP)

    alarms = if (alarmListStr.equals(ERROR_RESP)) {
        java.util.ArrayList()
    } else {
        alarmListStr?.let { convertJsonToAlarmList(it) }
    }
    return alarms
}

//get the alarms from locally
fun populateAlarmsFromLocally(context: Context): ArrayList<Alarm>? {
    val alarms: ArrayList<Alarm>?
    val alarmListStr = getStringInPreference(context, ALARM_LIST_KEY_PREF, ERROR_RESP)

    alarms = if (alarmListStr.equals(ERROR_RESP)) {
        ArrayList()
    } else {
        alarmListStr?.let { convertJsonToAlarmList(it) }
    }
    return alarms
}


//store the detectors to locally
fun storeAlarmsToLocally(alarms: java.util.ArrayList<Alarm>, context: Context) {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)
    // sort the list of events by date in descending
    val alarms = java.util.ArrayList(alarms.sortedWith(compareByDescending { it.timeInMillis }))
    if (alarms != null) {
        val alarmsJsonStr = convertToAlarmsGson(alarms)
        setStringInPreference(wContext.get(), ALARM_LIST_KEY_PREF, alarmsJsonStr)
    }
}

//store my email account locally
fun storeMyEmailAccountToLocaly(myEmailAccount: MyEmailAccount, context: Context) {
    //use WeakReference if the activity is no longer alive
    val wContext: WeakReference<Context> =
        WeakReference(context)

    if (myEmailAccount != null) {
        val myEmailAccountStr = convertToGson(myEmailAccount)
        setStringInPreference(wContext.get(), EMAIL_ACCOUNT_KEY, myEmailAccountStr)
    }
}

//set tags to locally
fun storeTagsToLocally(tags: JsonArray, context: Context) {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    setStringInPreference(wContext.get(), TAGS_KEY, tags.toString())
}

//get tags to locally
fun getTagsFromLocally(context: Context): ArrayList<String>? {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val tagsJ = getStringInPreference(wContext.get(), TAGS_KEY, null)
//    val js=JSONArray(tagsJ)
//    UserSession.instance.setTags(js)
    tagsJ?.let {
        val tags = convertJsonToStringList(it)
        return tags
    }
    return null
}


//set user info to locally
fun storeUserAzureToLocally(userInfo: UserInfoAzure, context: Context, key: String) {
    val jsonObject = Gson().toJson(userInfo)
    val wContext: WeakReference<Context> =
        WeakReference(context)
    setSecureStringInPreference(wContext.get(), key, jsonObject.toString())
}

// inspection fix: removed unused function

fun storeUserAmazonResultToLocally(userInfo: UserInfoAmazonResult, context: Context, key: String) {
    val jsonObject = Gson().toJson(userInfo)
    val wContext: WeakReference<Context> =
        WeakReference(context)
    setSecureStringInPreference(wContext.get(), key, jsonObject.toString())
}

//get user info azure to locally
fun getUserAzureFromLocally(context: Context, key: String): UserInfoAzure? {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val userJ = getSecureStringInPreference(wContext.get(), key)
    userJ?.let {
        val userInfo = convertJsonToUserInfoAzure(it)
        return userInfo
    }// Gson().fromJson<UserInfo>(userJ, UserInfo::class.java)

    return null
}

// inspection fix: removed unused function

//get user  info amazon to locally
fun getUserAmazonResultFromLocally(context: Context, key: String): UserInfoAmazonResult? {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val userJ = getSecureStringInPreference(context, key)
    userJ?.let {
        val userInfo = convertJsonToUserInfoResultAmazon(it)
        return userInfo
    }// Gson().fromJson<UserInfo>(userJ, UserInfo::class.java)

    return null
}


// inspection fix: removed empty writeFile() and its calls

// inspection fix: removed unused function

