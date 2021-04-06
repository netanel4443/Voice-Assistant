package com.e.VoiceAssistant.viewmodels.effects

import android.content.Intent
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData

sealed class TalkAndResultEffects {
    data class NavigateToDesiredApp(val intent: Intent):TalkAndResultEffects()
    data class NavigateToDesiredAppWithResults(val intent: Intent,val results:HashSet<ResultsData>,val dataType: Int):TalkAndResultEffects()
    data class HandleClick(val icon:Int):TalkAndResultEffects()
    object StartTimerAnimation:TalkAndResultEffects()
}