package com.e.VoiceAssistant.utils.rxJavaUtils

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

fun <T>Observable<T>.throttle(): Observable<T> =
  throttleFirst(1,TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())