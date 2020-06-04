package com.e.VoiceAssistant.ui.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

import javax.inject.Inject

abstract class BaseFragment: DaggerFragment() {

    private val compositeSubscription=CompositeDisposable()

    @Inject lateinit var provider: ViewModelProvider.Factory

    protected inline fun <reified T:ViewModel> getViewModel() : T =
         ViewModelProvider(requireActivity(),provider)[T::class.java]

    protected operator fun Disposable.unaryPlus(){
        compositeSubscription.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeSubscription.clear()
    }
}