package com.sensoguard.hunter.global

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.sensoguard.hunter.classes.Alarm
import com.sensoguard.hunter.classes.Camera
import com.sensoguard.hunter.classes.MyEmailAccount
import com.sensoguard.hunter.classes.SystemSort
import com.sensoguard.hunter.classes.UserInfoAmazon
import com.sensoguard.hunter.classes.UserInfoAzure
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


//convert camera to json
fun convertToGson(myEmailAccount: MyEmailAccount): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(myEmailAccount)
        val jsonElement = JsonParser().parse(data)
        return gSon.toJson(jsonElement)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        ex.message?.let { Log.i("exception", it) }
    } catch (ex: java.lang.Exception) {
        ex.message?.let { Log.i("exception", it) }
    }
    return ERROR_RESP
}


//convert camera to json
fun convertToGson(camera: Camera): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(camera)
        val jsonElement = JsonParser().parse(data)
        return gSon.toJson(jsonElement)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        ex.message?.let { Log.i("exception", it) }
    } catch (ex: java.lang.Exception) {
        ex.message?.let { Log.i("exception", it) }
    }
    return ERROR_RESP
}


//convert json to UserInfo azure
fun convertJsonToUserInfoAzure(inputJsonString: String): UserInfoAzure? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var userInfo: UserInfoAzure? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<UserInfoAzure>() {

        }.type
        userInfo = Gson().fromJson(json.toString(), listType) as UserInfoAzure
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return userInfo
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


//convert json to UserInfo amazon
fun convertJsonToUserInfoAmazon(inputJsonString: String): UserInfoAmazon? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var userInfo: UserInfoAmazon? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<UserInfoAmazon>() {

        }.type
        userInfo = Gson().fromJson(json.toString(), listType) as UserInfoAmazon
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return userInfo
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


//convert json to Camera
fun convertJsonToSensor(inputJsonString: String): Camera? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var mySensor: Camera? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<Camera>() {

        }.type
        mySensor = Gson().fromJson(json.toString(), listType) as Camera
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return mySensor
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


//convert json to Camera
fun convertJsonToMyEmailAccount(inputJsonString: String): MyEmailAccount? {

    //if the json string is empty, then return empty array list
    if (inputJsonString.isNullOrEmpty()) {
        return null
    }

    var myEmailAccount: MyEmailAccount? = null
    //mySensors?.add(Camera("ID","NAME"))

    var json: JSONObject? = null
    try {
        json = JSONObject(inputJsonString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<MyEmailAccount>() {

        }.type
        myEmailAccount = Gson().fromJson(json.toString(), listType) as MyEmailAccount
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return myEmailAccount
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


fun convertSystemSortToGson(detectorsArr: ArrayList<SystemSort>): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(detectorsArr)
        val jsonArray = JsonParser().parse(data).asJsonArray
        return gSon.toJson(jsonArray)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        ex.message?.let { Log.i("exception", it) }
    } catch (ex: java.lang.Exception) {
        ex.message?.let { Log.i("exception", it) }
    }
    return ERROR_RESP
}


fun convertToGson(detectorsArr: ArrayList<Camera>): String? {
    try {
        val gSon = Gson()
        val data = gSon.toJson(detectorsArr)
        val jsonArray = JsonParser().parse(data).asJsonArray
        return gSon.toJson(jsonArray)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        ex.message?.let { Log.i("exception", it) }
    } catch (ex: java.lang.Exception) {
        ex.message?.let { Log.i("exception", it) }
    }
    return ERROR_RESP
}

fun convertToAlarmsGson(alarmsArr:ArrayList<Alarm>): String? {
    try {
        val gSon= Gson()
        val data=gSon.toJson(alarmsArr)
        val jsonArray= JsonParser().parse(data).asJsonArray
        return gSon.toJson(jsonArray)
        //TODO : how to get response about set shared preference

    } catch (ex: JSONException) {
        ex.message?.let { Log.i("exception", it) }
    } catch (ex: java.lang.Exception) {
        ex.message?.let { Log.i("exception", it) }
    }
    return ERROR_RESP
}


//convert json to list of uri and list of Sensors
fun convertJsonToSystemSortList(inputJsonArrayString: String): ArrayList<SystemSort>? {

    //if the json string is empty, then return empty array list
    if (inputJsonArrayString.isNullOrEmpty()) {
        return ArrayList()
    }

    val mySensors: ArrayList<SystemSort>? = ArrayList()
    //mySensors?.add(Camera("ID","NAME"))

    var jsonArr: JSONArray? = null
    try {
        jsonArr = JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<List<SystemSort>>() {

        }.type
        mySensors?.addAll(Gson().fromJson(jsonArr.toString(), listType) as ArrayList<SystemSort>)
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return mySensors
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList

//convert json to list of uri and list of Sensors
fun convertJsonToSensorList(inputJsonArrayString: String): ArrayList<Camera>? {

    //if the json string is empty, then return empty array list
    if (inputJsonArrayString.isNullOrEmpty()) {
        return ArrayList()
    }

    val mySensors: ArrayList<Camera>? = ArrayList()
    //mySensors?.add(Camera("ID","NAME"))

    var jsonArr: JSONArray?=null
    try {
        jsonArr= JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<List<Camera>>() {

        }.type
        mySensors?.addAll(Gson().fromJson(jsonArr.toString(), listType) as ArrayList<Camera>)
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return mySensors
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList


//convert json to list of uri and list of Sensors
fun convertJsonToStringList(inputJsonArrayString: String): ArrayList<String>? {

    //if the json string is empty, then return empty array list
    if (inputJsonArrayString.isNullOrEmpty()) {
        return ArrayList()
    }

    val myTags: ArrayList<String>? = ArrayList()
    //mySensors?.add(Camera("ID","NAME"))

    var jsonArr: JSONArray? = null
    try {
        jsonArr = JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<List<String>>() {

        }.type
        myTags?.addAll(Gson().fromJson(jsonArr.toString(), listType) as ArrayList<String>)
    } catch (e: JsonIOException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    return myTags
    //when jsonArr is null will return value of new ArrayList<>()
}//convertJsonToUriList

//convert json to list of uri and list of Alarms
fun convertJsonToAlarmList(inputJsonArrayString: String): ArrayList<Alarm>? {

    var myAlarms: ArrayList<Alarm>? = null
    var jsonArr: JSONArray? = null
    try {
        jsonArr = JSONArray(inputJsonArrayString)
    } catch (e: JSONException) {
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToUriList", it) }
    }

    try {
        val listType = object : TypeToken<List<Alarm>>() {

        }.type
        myAlarms = Gson().fromJson(jsonArr.toString(), listType) as ArrayList<Alarm>
    }catch(e:JsonIOException){
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToAlarmList", it) }
    }catch(e:JsonSyntaxException){
        e.printStackTrace()
        e.message?.let { Log.e("convertJsonToAlarmList", it) }
    }

    return myAlarms
    //when jsonArr is null will return value of new ArrayList<>()


}