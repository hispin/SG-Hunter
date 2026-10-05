package com.sensoguard.hunter.global

import android.content.Context
import androidx.core.content.edit


//read Long ic_share preference
fun getLongInPreference(context: Context?, key: String, default: Long): Long {
    if (context == null) {
        return -1
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    return pref.getLong(key, default)
}


//write Long to ic_share preference
fun setLongInPreference(context: Context?, key: String, value: Long) {
    if (context == null) {
        return
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    pref.edit { putLong(key, value) } // lint fix: KTX edit
}


//write String to ic_share preference
fun setStringInPreference(context: Context?, key: String, value: String?) {
    if (context == null) {
        return
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    pref.edit { putString(key, value) } // lint fix: KTX edit
}

//remove ic_share preference
fun removePreference(context: Context?, key: String) {
    if (context == null) {
        return
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    pref.edit { remove(key) } // lint fix: KTX edit
}


//read String ic_share preference
fun getStringInPreference(context: Context?, key: String, default: String?): String? {
    if (context == null) {
        return null
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    return pref.getString(key, default)
}

// inspection fix: removed unused duplicate of removePreference()

//read Boolean ic_share preference
fun getBooleanInPreference(context: Context?, key: String, default: Boolean): Boolean {
    if (context == null) {
        return false
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    return pref.getBoolean(key, default)
}


//write Boolean to ic_share preference
fun setBooleanInPreference(context: Context?, key: String, value: Boolean) {
    if (context == null) {
        return
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    pref.edit { putBoolean(key, value) } // lint fix: KTX edit
}


//read Int ic_share preference
fun getIntInPreference(context: Context?, key: String, default: Int): Int? {
    if (context == null) {
        return null
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    return pref.getInt(key, default)
}


//write Int to ic_share preference
fun setIntInPreference(context: Context?, key: String, value: Int) {
    if (context == null) {
        return
    }
    val pref=context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE)
    pref.edit { putInt(key, value) } // lint fix: KTX edit
}

