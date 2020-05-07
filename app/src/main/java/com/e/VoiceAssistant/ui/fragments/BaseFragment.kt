package com.e.VoiceAssistant.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

abstract class BaseFragment: DaggerFragment() {

    private val compositeSubscription=CompositeSubscription()

    @Inject lateinit var provider: ViewModelProvider.Factory

    protected inline fun <reified T:ViewModel> getViewModel() : T =
         ViewModelProvider(requireActivity(),provider)[T::class.java]

    protected operator fun Subscription.unaryPlus(){
        compositeSubscription.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }
}