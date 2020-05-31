package com.e.VoiceAssistant.viewmodels

import android.content.pm.PackageManager
import android.content.res.Resources
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.usecases.LoadDataUseCase
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.viewmodels.commands.LoadDataCommands
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class LoadDataViewModel @Inject constructor(
    private val useCases: LoadDataUseCase):BaseViewModel() {
    private val _commands=PublishSubject.create<LoadDataCommands>()
    val commands get() = _commands

   fun getData(pm:PackageManager,resources: Resources){
        val first=useCases.getAppsDetails(pm)
            .doOnSuccess { _commands.onNext(LoadDataCommands.GetDeviceApps(it)) }
            .flatMap(::getStoredAppsDetails)
            .doOnSuccess {_commands.onNext(LoadDataCommands.StoredAppsDetails(it))  }
            .toObservable()
        val second=useCases.getCurrentLocale(resources)
            .doOnNext{_commands.onNext(LoadDataCommands.GetCurrentLocaleDigits(it))}
        val observableList= arrayListOf(first,second)

       +Observable.combineLatest(observableList){}
           .subscribeOnIoAndObserveOnMain()
           .subscribe{_commands.onNext(LoadDataCommands.LoadComplete)}
   }

    private fun getStoredAppsDetails(hashMap: HashMap<String, AppsDetails>):Single<HashMap<String,AppsDetails>> {
        return useCases.getAppsListFromDB()
            .flatMap { useCases.addToSavedAppsProperIcon(it,hashMap) }
            .subscribeOnIoAndObserveOnMain()
    }
}