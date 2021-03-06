package com.e.VoiceAssistant.usecases.commons

import android.content.Intent
import android.speech.RecognizerIntent
import io.reactivex.Observable
import io.reactivex.Single

class RecognizerIntentInit {

    fun init(): Observable<Intent> {
      return  Observable.fromCallable {
            val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
         //   intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            intent.putExtra("android.speech.extra.DICTATION_MODE", true)
            //  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US")
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100)
            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, arrayOf("he","en-US"))
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en-US")
            intent
      }
    }
}