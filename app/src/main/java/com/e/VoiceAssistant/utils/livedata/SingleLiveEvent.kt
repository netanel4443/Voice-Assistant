package com.e.VoiceAssistant.utils.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> :MutableLiveData<T>() {
    private val pendings=HashMap<Observer<*>, AtomicBoolean>()

    override fun  observe(owner: LifecycleOwner, observer: Observer<in T>) {
        pendings[observer]= AtomicBoolean(false)

        // Observe the internal MutableLiveData
        super.observe(owner, Observer<T> { t ->
            if (pendings[observer]!!.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    override fun setValue( t: T?) {
        pendings.entries.forEach {it.setValue(AtomicBoolean(true))}
        super.setValue(t)
    }
}