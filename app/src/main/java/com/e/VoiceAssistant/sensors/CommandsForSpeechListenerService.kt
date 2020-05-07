package com.e.VoiceAssistant.sensors

interface CommandsForSpeechListenerService {

    fun onError(message:String)
    fun changeTalkIcon()
    fun checkForResults(matches:ArrayList<String>)

}