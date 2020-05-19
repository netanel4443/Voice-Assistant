package com.e.VoiceAssistant.presenters

import android.view.Gravity
import android.view.View
import com.e.VoiceAssistant.di.annotations.ServiceScope
import com.e.VoiceAssistant.presenters.presentersStates.SpeechRecognizerServicePresenterState
import com.e.VoiceAssistant.usecases.SpeechRecognizerUseCases
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

@ServiceScope
class SpeechRecognizerServicePresenter @Inject constructor(
    private val useCases:SpeechRecognizerUseCases) {
    private val compositeDisposable=CompositeDisposable()

    lateinit var view:SpeechRecognizerServicePresenterState

    fun bindView(view:SpeechRecognizerServicePresenterState){ this.view=view }

    fun initWindowManager(){
        val params=useCases.windowManagerAttributes()
        params.gravity = Gravity.TOP or Gravity.LEFT
        val darkScreenParams=useCases.windowManagerFullScreenAttributes()
        view.initWindowManager(params,darkScreenParams)
    }

    fun openSettingsActivity() {
        view.openSettingsActivity()
    }


    fun handleMenuClick(visibility: Int) {
        if (visibility==View.GONE)
            view.handleMenuClick(View.VISIBLE)
        else
            view.handleMenuClick(View.GONE)
    }

    private operator fun Disposable.unaryPlus(){
        compositeDisposable.add(this)
    }

    fun dispose(){
        compositeDisposable.clear()
    }
}