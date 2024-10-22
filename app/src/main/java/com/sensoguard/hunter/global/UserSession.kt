package com.sensoguard.hunter.global

import com.sensoguard.hunter.classes.AlarmSensor
import com.sensoguard.hunter.classes.UserInfoAmazon
import com.sensoguard.hunter.classes.UserInfoAmazonResult
import com.sensoguard.hunter.classes.UserInfoAzure

class UserSession private constructor() {

    private var userInfoAmazonResult: UserInfoAmazonResult?=null
    private var userInfoAzure: UserInfoAzure? = null
    private var userInfoAmazon: UserInfoAmazon? = null

    private var tags: ArrayList<String>? = null

    //list of sensors alarm
    var alarmSensors: ArrayList<AlarmSensor>? = ArrayList()

    private object Holder {
        val INSTANCE = UserSession()
    }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }

    fun getUserAzure(): UserInfoAzure? {
        return userInfoAzure
    }

    fun getUserAmazon(): UserInfoAmazon? {
        return userInfoAmazon
    }

    fun getUserAmazonResult(): UserInfoAmazonResult? {
        return userInfoAmazonResult
    }

    fun getTags(): ArrayList<String>? {
        return tags
    }

    fun setInstanceUserAzure(name: String, pw: String) {
        userInfoAzure = UserInfoAzure(
            name,
            pw
        )
    }

    fun setInstanceUserAmazon(name: String, pw: String, token: String) {
        userInfoAmazon = UserInfoAmazon(
            name,
            pw,
            token
        )
    }

    fun setInstanceUserAmazonResult(name: String?, pw: String?, token_fcm: String?,token: String?,imagesBaseUrl:String?,role:Int?) {
        userInfoAmazonResult = UserInfoAmazonResult(
            name,
            pw,
            token_fcm,
            token,
            imagesBaseUrl,
            role
        )
    }

    fun setInstanceUserAzure(userInfo: UserInfoAzure) {
        this.userInfoAzure = userInfo
    }

    fun setInstanceUserAmazon(userInfo: UserInfoAmazon) {
        this.userInfoAmazon = userInfo
    }

    fun setInstanceUserAmazonResult(userInfo: UserInfoAmazonResult) {
        this.userInfoAmazonResult = userInfo
    }

    fun setTags(tags: ArrayList<String>) {
        this.tags = tags
    }


}