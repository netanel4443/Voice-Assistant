package com.e.VoiceAssistant.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.ui.dialogs.CircleProgressBarDialog
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

abstract class BaseActivity:DaggerAppCompatActivity() {

    protected lateinit var progressBar:CircleProgressBarDialog
    protected val compositeSubscription=CompositeSubscription()
    protected val compositeDisposable=CompositeDisposable()

    @Inject lateinit var provider:ViewModelProvider.Factory

    protected inline fun <reified T : ViewModel> getViewModel(): T =
       ViewModelProvider(this , provider)[T::class.java]

    protected inline operator fun <reified T : Subscription> T.unaryPlus() =
        compositeSubscription.add(this)

    protected inline operator fun <reified T : Disposable> T.unaryPlus() =
        compositeDisposable.add(this)

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
        compositeDisposable.clear()
    }
}