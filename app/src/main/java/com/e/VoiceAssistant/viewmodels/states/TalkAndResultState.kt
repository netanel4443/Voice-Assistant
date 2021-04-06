package com.e.VoiceAssistant.viewmodels.states

import android.content.Intent
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData

data class TalkAndResultState(
    var timerValue:Int=3,
    var talkBtnIcon:Int= R.drawable.ic_mic_white_background_24, //default icon
    var speechResults: SpeechResults= SpeechResults(HashSet(),0)
//    ,
//    var speechResultsWithIntent:SpeechResultsWithIntent= SpeechResultsWithIntent(HashSet(),0, Intent())

){
    data class SpeechResults(val results:HashSet<ResultsData>,val dataType:Int)
//    data class SpeechResultsWithIntent(val results:HashSet<ResultsData>,val dataType:Int,val intent: Intent)
}

