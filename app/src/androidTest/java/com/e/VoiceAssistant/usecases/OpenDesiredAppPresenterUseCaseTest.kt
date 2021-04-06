package com.e.VoiceAssistant.usecases

import android.content.ComponentName
import android.content.Intent
import com.e.VoiceAssistant.data.AppsDetails
import io.reactivex.Observable
import org.junit.Test
import org.junit.Assert.*

class OpenDesiredAppPresenterUseCaseTest {

    @Test
    fun getDesiredIntent() {
        val odap=OpenDesiredAppPresenterUseCase()
        val fakeAppDetails=AppsDetails(
            realName = "voice assistant",
            pckg = "com.e.VoiceAssistant"
        )
        val fakeAppComponentMap= hashMapOf(Pair("voice assistant", fakeAppDetails))
        val fakeSplitedResult= linkedSetOf("blah","voice","assistant")
        val result=odap.getDesiredIntent(fakeAppComponentMap, fakeSplitedResult)


        var fakeIntent:Intent=Intent()
         fakeAppComponentMap["voice assistant"]?.run {
           val component= ComponentName(this.activity, this.pckg)
           fakeIntent= Intent(Intent.ACTION_MAIN)
           fakeIntent.addCategory(Intent.CATEGORY_LAUNCHER)
           fakeIntent.component =component
           fakeIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
           fakeIntent
        }
        // get our intent
        val desiredIntent=result.test().values()[0]
        val areIntentsTheSame= desiredIntent.action==fakeIntent.action &&
            desiredIntent.component==fakeIntent.component  &&
            desiredIntent.flags==fakeIntent.flags &&
            desiredIntent.categories==fakeIntent.categories

        assertTrue(areIntentsTheSame)
    }

}