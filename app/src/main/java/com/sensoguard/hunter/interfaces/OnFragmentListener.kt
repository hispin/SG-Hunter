package com.sensoguard.hunter.interfaces

import com.sensoguard.hunter.classes.Alarm

interface OnFragmentListener {
    fun updateLanguage()
    fun onSaveForShareVideo(alarm: Alarm)

    fun onSaveForShareVideo(imgPath: String)

    fun onBack()
}