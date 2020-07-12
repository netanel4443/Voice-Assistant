package com.e.VoiceAssistant.viewmodels

import android.content.ContentResolver
import android.content.pm.PackageManager
import android.content.res.Resources
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.usecases.ContactListUseCase
import com.e.VoiceAssistant.usecases.LoadDataUseCase
import com.e.VoiceAssistant.userscollecteddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.viewmodels.commands.LoadDataCommands
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.Serializable
import javax.inject.Inject

class LoadDataViewModel @Inject constructor(
    private val useCases: LoadDataUseCase,
    private val contactsUseCase:ContactListUseCase,
    private val globalData:AppsDetailsSingleton):BaseViewModel() {

    private val TAG=this::class.simpleName
    private val _commands=PublishSubject.create<LoadDataCommands>()
    val commands get() = _commands
    private val dataObservableList= arrayListOf<Observable<out Serializable>>()

   fun getData(pm:PackageManager,resources: Resources){

        val first=useCases.getAppsDetails(pm)
            .doOnSuccess {apps->
                globalData.appsDetailsHmap.putAll(apps)
                globalData.appsAndStoredAppsDetails.putAll(apps)
            }
            .flatMap(::getStoredAppsDetails)
            .doOnSuccess {apps->
                globalData.storedAppsDetailsFromDB.putAll(apps)
                globalData.appsAndStoredAppsDetails.putAll(apps)
            }
            .toObservable()

        val second=useCases.getCurrentLocale(resources)
            .doOnNext{digits-> globalData.countryLocaleDigits=digits}

            dataObservableList.add(first)
            dataObservableList.add(second)

       +Observable.combineLatest(dataObservableList){}
           .subscribeOnIoAndObserveOnMain()
           .subscribe(
               {_commands.onNext(LoadDataCommands.LoadComplete)},
               { printIfDebug(TAG,it.message)}
           )
   }

   fun addCotnactObservable(cr:ContentResolver){
       val contactsObservable=contactsUseCase.getContacts(cr)
           .doOnNext{contacts->
               globalData.contactList.putAll(contacts)
           }
       dataObservableList.add(contactsObservable)
   }

    private fun getStoredAppsDetails(hashMap: HashMap<String, AppsDetails>):Single<HashMap<String,AppsDetails>> {
        return useCases.getAppsListFromDB()
            .flatMap { useCases.addToSavedAppsProperIcon(it,hashMap) }
            .subscribeOnIoAndObserveOnMain()
    }

}