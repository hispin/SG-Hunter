package com.sensoguard.hunter.global

import android.content.Context
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.sensoguard.hunter.R

fun ToastNotify(notificationMessage: String?, context: Context) {
    Toast.makeText(context, notificationMessage, Toast.LENGTH_LONG).show()
}

//check if the field of edit text is empty
fun validIsEmpty(editText: EditText?, context: Context): Boolean {
    var isValid = true

    if (editText?.text.isNullOrBlank()) {
        editText?.error = context.resources.getString(R.string.empty_field_error)
        isValid = false
    }

    return isValid
}

fun showToast(context: Context?, msg: String) {
    if (context == null) return
    Toast.makeText(
        context,
        msg,
        Toast.LENGTH_LONG
    )
        .show()
}

/**
 * check if the app has battery restriction of accepting notifications in background
 */
fun checkBackgroundNotifRestrict(context: Context) = when {
    NotificationManagerCompat.from(context).areNotificationsEnabled() -> true
    else -> false
}