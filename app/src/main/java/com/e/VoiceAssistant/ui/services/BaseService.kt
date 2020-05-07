package com.e.VoiceAssistant.ui.services

import dagger.android.DaggerService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import rx.Subscription
import rx.subscriptions.CompositeSubscription

abstract class BaseService:DaggerService() {

    protected val compositeSubscription= CompositeSubscription()
    protected val compositeDisposable= CompositeDisposable()

    protected inline operator fun<reified T :Subscription> T.unaryPlus() =
        compositeSubscription.add(this)

    protected inline operator fun<reified T :Disposable> T.unaryPlus() =
        compositeDisposable.add(this)


    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
        compositeDisposable.clear()
    }


}