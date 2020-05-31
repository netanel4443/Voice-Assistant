package com.e.VoiceAssistant.presenters.presentersStates

import android.content.Intent
import android.view.WindowManager
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData

interface SpeechRecognizerServicePresenterState {
   fun initWindowManager(params: WindowManager.LayoutParams,
                         trashParams: WindowManager.LayoutParams )
   fun openSettingsActivity()
   fun showOrDismissTrash(visibility: Int)
}