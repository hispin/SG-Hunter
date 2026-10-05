package com.sensoguard.hunter.global

// inspection fix: removed unused import(s)
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun getStringFromCalendar(calendar: Calendar,format:String,context: Context):String{
    // lint fix: minSdk is 26, so locales is always available
    val locale=context.resources.configuration.locales.getFirstMatch(context.resources.assets.locales)
    val dateFormat = SimpleDateFormat(format, locale)//"kk:mm dd/MM/yy"
    //dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val dateString = dateFormat.format(calendar.time)
    return dateString
}

//get string format by current date time in milliseconds format
fun getStrDateTimeByMilliSeconds(milliSeconds: Long, format: String, context: Context): String? {
    // lint fix: minSdk is 26, so locales is always available
    val locale =
        context.resources.configuration.locales.getFirstMatch(context.resources.assets.locales)
    if (locale != null) {
        val dateFormat = SimpleDateFormat(format, locale)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(milliSeconds)
        return dateFormat.format(date)
    }
    return null
}

// inspection fix: removed unused getDateByTimestamp()
