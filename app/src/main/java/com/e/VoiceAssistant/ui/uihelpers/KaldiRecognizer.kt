package com.e.VoiceAssistant.ui.uihelpers

import com.e.VoiceAssistant.utils.printIfDebug
import org.kaldi.Model
import java.io.IOException

class KaldiRecognizer(val action:(KaldiActions)->Unit) :org.kaldi.RecognitionListener {
    var once=true
    private var recognizer: org.kaldi.SpeechRecognizer? = null
    private val TAG="KaldiRecognizer"

    override fun onResult(hypothesis: String?) {}

    override fun onPartialResult(hypothesis: String) {
         printIfDebug(TAG,hypothesis)
        if (hypothesis.contains("listen")&&once) {
            once=false
            action(KaldiActions.DETECTED)
        }
    }

    override fun onTimeout() {
        recognizer?.cancel()
        recognizer = null
    }

    override fun onError(e: Exception) {
        action(KaldiActions.ERROR)
        printIfDebug("on eror"+TAG,e.message)
    }

    fun stopRecognizer(){
        recognizer?.cancel()
    }

    fun startListening(){
        recognizer?.startListening()
    }

     fun recognizeMicrophone(model: Model) {
        if (recognizer != null) {
            recognizer?.cancel()
            recognizer = null
        } else {
            try {
                recognizer = org.kaldi.SpeechRecognizer(model)
                recognizer?.addListener(this)
                recognizer?.startListening()
            } catch (e: IOException) {
             printIfDebug("recognize"+TAG,e.message)
            }
        }
    }

    fun onStart(){
        recognizer?.let{
            it.startListening()
            it.addListener(this)
        }
    }

    fun onStop(){
        recognizer?.let{
            it.cancel()
            it.removeListener(this)
        }
    }

    fun onDestroy() {
        recognizer?.let {
            it.cancel()
            it.shutdown()
        }
    }

    enum class KaldiActions {DETECTED,ERROR}
}