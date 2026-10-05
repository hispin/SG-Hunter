package com.sensoguard.hunter.global

// inspection fix: removed unused import(s)
import android.content.Context
import android.content.res.Configuration
import java.util.Locale


//Get the current language of the app
fun getAppLanguage(): String {
    return Locale.getDefault().language
}

//set language for the application
fun setAppLanguage(c: Context, lang: String) {
    val localeNew = Locale(lang)
    Locale.setDefault(localeNew)

    val res = c.resources
    val newConfig = Configuration(res.configuration)
    newConfig.locale = localeNew
    newConfig.setLayoutDirection(localeNew)
    res.updateConfiguration(newConfig, res.displayMetrics)

    // lint fix: minSdk is 26, so the SDK check was removed
    newConfig.setLocale(localeNew)
    c.createConfigurationContext(newConfig)
}




