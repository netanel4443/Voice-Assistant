package com.e.VoiceAssistant.utils.livedata

import androidx.lifecycle.MutableLiveData

class MviMutableLiveData<V, T: Pair<V,V>>(): MutableLiveData<T>() {

    fun setMviValue(v:V){
        val pair=Pair(v,v)
        super.setValue(pair as T)
    }

}