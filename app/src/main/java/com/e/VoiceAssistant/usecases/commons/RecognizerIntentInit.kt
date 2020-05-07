package com.e.VoiceAssistant.usecases.commons

import android.content.Intent
import android.speech.RecognizerIntent
import io.reactivex.Single

class RecognizerIntentInit {

    fun init():Single<Intent>{
      return  Single.fromCallable {
            val intent= Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            //  intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US")
            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES,"en-US")
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, arrayOf("he","en-US"))
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en-US")
        }
    }
}