package com.e.VoiceAssistant.viewmodels

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseViewModel:ViewModel() {

   private val compositeDisposable=CompositeDisposable()

   protected operator fun Disposable.unaryPlus(){
       compositeDisposable.add(this)
   }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}