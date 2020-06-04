package com.e.VoiceAssistant.utils.rxJavaUtils

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


fun <T>Observable<T>.throttle() =
    throttleFirst(1,TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())