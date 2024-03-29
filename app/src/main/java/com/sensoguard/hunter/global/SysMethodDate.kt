package com.sensoguard.hunter.global

import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

fun getStringFromCalendar(calendar: Calendar,format:String,context: Context):String{
    val locale=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.resources.configuration.locales.getFirstMatch(context.resources.assets.locales)
    else context.resources.configuration.locale
    val dateFormat = SimpleDateFormat(format, locale)//"kk:mm dd/MM/yy"
    //dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val dateString = dateFormat.format(calendar.time)
    return dateString
}

//get string format by current date time in milliseconds format
fun getStrDateTimeByMilliSeconds(milliSeconds: Long, format: String, context: Context): String? {
    val locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            context.resources.configuration.locales.getFirstMatch(context.resources.assets.locales)
        else
            context.resources.configuration.locale
    if (locale != null) {
        val dateFormat = SimpleDateFormat(format, locale)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = Date(milliSeconds)
        return dateFormat.format(date)
    }
    return null
}

/**
 * An overloaded version of getDateByTimestamp by String
 *
 * @return String "yyyy:MM:dd HH:mm:ss"
 */
fun getDateByTimestamp(
    stringDate: String?,
    context: Context,
    format: String
): Calendar? {

    val locale =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) context.resources.configuration.locales.getFirstMatch(
            context.resources.assets.locales
        )
        else context.resources.configuration.locale

    val dateFormat_yyyyMMddHHmmss = SimpleDateFormat(
        format, locale
    )
    val date = dateFormat_yyyyMMddHHmmss.parse(stringDate)
    val calendar = Calendar.getInstance()
    calendar.time = date
    return calendar
}