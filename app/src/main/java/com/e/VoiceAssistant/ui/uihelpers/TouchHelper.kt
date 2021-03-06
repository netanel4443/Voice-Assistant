package com.e.VoiceAssistant.ui.uihelpers

sealed class TouchHelper {

    object downEvent : TouchHelper()
    data class moveEvent(val xPos:Int,val yPos:Int):TouchHelper()
    object TalkOrStopClickEvent : TouchHelper()
    object upEvent : TouchHelper()
    object StopService : TouchHelper()
}