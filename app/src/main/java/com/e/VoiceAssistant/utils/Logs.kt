package com.e.VoiceAssistant.utils

import com.e.VoiceAssistant.utils.Debug.DBG

object Debug { const val DBG=false }

fun printIfDebug(TAG: String?, message:String?){
    if (DBG){ println("$TAG : $message")
    }
}