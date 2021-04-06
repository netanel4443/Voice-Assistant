package com.e.VoiceAssistant.utils.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


    fun <T> LiveData<T>.toObservable(lifecycleOwner: LifecycleOwner): Observable<T> = Observable
        .fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, this))

    fun <T> Flowable<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

    fun <T> BehaviorSubject<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this.toFlowable(BackpressureStrategy.BUFFER))
