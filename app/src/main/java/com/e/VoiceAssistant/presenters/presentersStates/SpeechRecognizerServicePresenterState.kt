package com.e.VoiceAssistant.presenters.presentersStates

import android.content.Intent
import android.view.WindowManager

interface SpeechRecognizerServicePresenterState {

   fun navigateToDesiredApp(intent: Intent)
   fun initWindowManager(params: WindowManager.LayoutParams,
                         trashParams: WindowManager.LayoutParams )
   fun handleClick(icon : Int)
   fun openSettingsActivity()
   fun handleMenuClick(visibility: Int)
   fun changeTalkIcon(icon:Int)
   fun secondListenToUser()
}