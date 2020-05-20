package com.e.VoiceAssistant.utils.rxJavaUtils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


fun <T>Single<T>.subscribeOnIoAndObserveOnMain():Single<T> =
        subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


fun Completable.subscribeOnIoAndObserveOnMain():Completable=
        subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

fun <T>Observable<T>.subscribeOnIoAndObserveOnMain():Observable<T> =
        subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

fun <T>Observable<T>.subscribeOnIoAndObserveOnMainSubscribe(success:(T)->Unit,error:(Throwable)->Unit):Disposable=
        subscribeOnIoAndObserveOnMain()
        .subscribe(success,error)








