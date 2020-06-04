package com.e.VoiceAssistant.ui.services

import dagger.android.DaggerService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseService:DaggerService() {

    protected val compositeDisposable= CompositeDisposable()

    protected inline operator fun<reified T :Disposable> T.unaryPlus() =
        compositeDisposable.add(this)

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


}