package com.e.VoiceAssistant.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.usecases.LoadDataUseCase
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.viewmodels.commands.LoadDataCommands
import javax.inject.Inject

class LoadDataViewModel @Inject constructor(
    private val useCases: LoadDataUseCase):BaseViewModel() {
    private val _commands=MutableLiveData<LoadDataCommands>()
    val commands:LiveData<LoadDataCommands> get() =_commands

    fun getStoredAppsDetails(hashMap: HashMap<String, AppsDetails>) {
        +useCases.getAppsListFromDB()
            .flatMap { useCases.addToSavedAppsProperIcon(it,hashMap) }
            .subscribeOnIoAndObserveOnMain()
            .subscribe({
                _commands.value=LoadDataCommands.StoredAppsDetails(it)
            },{
                /*  it.printStackTrace()*/
            })
    }
}