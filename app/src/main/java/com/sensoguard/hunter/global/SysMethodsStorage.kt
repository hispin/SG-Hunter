package com.sensoguard.hunter.global

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.MyEmailAccount
import com.sensoguard.hunter.classes.UserInfoAmazon
import com.sensoguard.hunter.classes.UserInfoAzure
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

//get myEmail from locally
fun getMyEmailAccountFromLocally(context: Context): MyEmailAccount? {

    val myEmailAccountStr = getStringInPreference(context, EMAIL_ACCOUNT_KEY, null)
    myEmailAccountStr?.let {
        var myEmailAccount = convertJsonToMyEmailAccount(myEmailAccountStr)
        return myEmailAccount
    }
    return null
}

//store the sensors to locally
fun storeSensorsToLocally(sensors: ArrayList<Camera>, context: Context) {

    var detectorsJsonStr:String?=""
    if(sensors!=null && sensors.size>0){
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

//store the my email account to locally
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
    setStringInPreference(wContext.get(), key, jsonObject.toString())
}

fun storeUserAmazonToLocally(userInfo: UserInfoAmazon, context: Context, key: String) {
    val jsonObject = Gson().toJson(userInfo)
    val wContext: WeakReference<Context> =
        WeakReference(context)
    setStringInPreference(wContext.get(), key, jsonObject.toString())
}

//get user info azure to locally
fun getUserAzureFromLocally(context: Context, key: String): UserInfoAzure? {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val userJ = getStringInPreference(wContext.get(), key, null)
    userJ?.let {
        val userInfo = convertJsonToUserInfoAzure(it)
        return userInfo
    }// Gson().fromJson<UserInfo>(userJ, UserInfo::class.java)

    return null
}

//get user  info amazon to locally
fun getUserAmazonFromLocally(context: Context, key: String): UserInfoAmazon? {
    val wContext: WeakReference<Context> =
        WeakReference(context)
    val userJ = getStringInPreference(context, key, null)
    userJ?.let {
        val userInfo = convertJsonToUserInfoAmazon(it)
        return userInfo
    }// Gson().fromJson<UserInfo>(userJ, UserInfo::class.java)

    return null
}


//write to log
fun writeFile(logMsg: String, context: Context) {
//    val thread = object : Thread() {
//        override fun run() {
//            val externalStorageDir = Environment.getExternalStorageDirectory()
//            val myFile = File(externalStorageDir, "hunter_logs.txt")
//
//            if (myFile.exists()) {
//                try {
//                    val currDateStr =
//                        getStringFromCalendar(Calendar.getInstance(), "dd/MM/yy kk:mm:ss", context)
//                    val fostream = FileOutputStream(myFile, true)
//                    val oswriter = OutputStreamWriter(fostream)
//                    val bwriter = BufferedWriter(oswriter)
//                    bwriter.write("$currDateStr:log:$logMsg")
//                    bwriter.newLine()
//                    bwriter.close()
//                    oswriter.close()
//                    fostream.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            } else {
//                try {
//                    myFile.createNewFile()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//
//        }
//    }
//
//    thread.start()


}

//convert bitmap to Uri
fun getImageUriByBitmap2(inContext: Context, inImage: Bitmap): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        var file = File(inContext.filesDir, "attached.jpeg")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))

        var contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, file.path)
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)

        return inContext.contentResolver.insert(Uri.fromFile(file), contentValues)
    } else {
        var bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        var path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
}

