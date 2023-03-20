package com.sensoguard.hunter.global

import com.sensoguard.hunter.classes.AlarmSensor
import com.sensoguard.hunter.classes.UserInfo

class UserSession private constructor() {

    private var userInfo: UserInfo? = null

    private var tags: ArrayList<String>? = null

    //list of sensors alarm
    var alarmSensors: ArrayList<AlarmSensor>? = ArrayList()

    private object Holder {
        val INSTANCE = UserSession()
    }

    companion object {
        val instance: UserSession by lazy { Holder.INSTANCE }
    }

    fun getUser(): UserInfo? {
        return userInfo
    }

    fun getTags(): ArrayList<String>? {
        return tags
    }

    fun setInstanceUser(name: String, pw: String) {
        userInfo = UserInfo(
            name,
            pw
        )
    }

    fun setInstanceUser(userInfo: UserInfo) {
        this.userInfo = userInfo
    }

    fun setTags(tags: ArrayList<String>) {
        this.tags = tags
    }


}