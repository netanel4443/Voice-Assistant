package com.e.VoiceAssistant.presenters.presentersStates

import android.content.Intent
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData

interface TalkAndResultsPresenterView {
    enum class dataType{CONTACTS,MATCHES}
    fun navigateToDesiredApp(intent: Intent, results:HashSet<ResultsData>, dataType: Int)
    fun navigateToDesiredApp(intent: Intent)
    fun handleClick(icon : Int)
    fun openSettingsActivity()
    fun changeTalkIcon(icon:Int)
    fun secondListenToUser(contacts:HashSet<ResultsData>, dataType: Int, intent: Intent)
    fun timerAnimation(counter:Int)
    fun showResults(results:HashSet<ResultsData>, dataType: Int)
    fun muteOrUnmute() {}
}