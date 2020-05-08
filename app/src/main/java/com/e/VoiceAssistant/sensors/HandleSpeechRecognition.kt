package com.e.VoiceAssistant.sensors

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import com.e.VoiceAssistant.utils.printMessage

class HandleSpeechRecognition(private val view:CommandsForSpeechListenerService):RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onPartialResults(partialResults: Bundle?) {}

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val message: String = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> ""
            SpeechRecognizer.ERROR_CLIENT -> ""
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> ""
            SpeechRecognizer.ERROR_NETWORK -> ""
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> ""
            SpeechRecognizer.ERROR_NO_MATCH -> ""
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "ERROR_RECOGNIZER_BUSY"
            SpeechRecognizer.ERROR_SERVER -> ""
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> ""
            else -> "Didn't understand, please try again."
        }
        printMessage("error",message)

        if (message!="ERROR_RECOGNIZER_BUSY") {
            view.changeTalkIcon()
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!!
            matches.forEach{
            println("matches $it")
        }
        view.changeTalkIcon()
        view.checkForResults(matches)
    }
}
