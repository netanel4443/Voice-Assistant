package com.e.VoiceAssistant.presenters

import android.content.Intent
import android.view.Gravity
import android.view.View
import com.e.VoiceAssistant.ComponentObject
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.presenters.presentersStates.SpeechRecognizerServicePresenterState
import com.e.VoiceAssistant.usecases.PresenterUseCases
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SpeechReconizerServicePresenter@Inject constructor(
    private val useCases:PresenterUseCases){

    private val compositeDisposable=CompositeDisposable()
    private var addedApps= HashMap<String, Pair<String,String>>()//<newName,realName>
    private var appsToDelete=ArrayList<String>()
    private var talkBtnIcon=R.drawable.ic_mic_white_background_24

    private val clickSubject= PublishSubject.create<Int>()

    init {
        +clickSubject.throttle()
            .doOnNext { view.handleClick(it) }
            .subscribe({},{})
    }

    lateinit var view:SpeechRecognizerServicePresenterState

    fun bindView(view:SpeechRecognizerServicePresenterState){ this.view=view }



    fun returnRequiredOperationIntent(matches: ArrayList<String>,
                                      appComponent: HashMap<String, ComponentObject>,
                                      contactList: HashMap<String,String> ){

        +useCases.returnRequiredOperationIntent(matches,appComponent,contactList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({intent->
              view.navigateToDesiredApp(intent)
            },{})
    }

    fun initWindowManager(){
       val params=useCases.windowManagerAttributes()
           params.gravity = Gravity.TOP or Gravity.LEFT
       val darkScreenParams=useCases.windowManagerFullScreenAttributes()
       view.initWindowManager(params,darkScreenParams)
    }

     fun getAppsList(appComponents:HashMap<String, ComponentObject>){
        +useCases.getAppsListFromDB()
            .subscribeOnIoAndObserveOnMain()
            .subscribe(
                {
                    +useCases.extractAddedAppsFromAppList(it,appComponents.keys )
                        .subscribeOnIoAndObserveOnMain()
                        .subscribe({
                            addedApps=it.first
                            view.addAppsFromMemory(addedApps)
                            appsToDelete=it.second
                            deleteNonExistApps()
                        },{ //it.printStackTrace()
                             })
                },
                { //it.printStackTrace()
                })
    }

    private fun deleteNonExistApps(){
        +useCases.deleteApps(appsToDelete)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({},{
             //   it.printStackTrace()
            })
    }


    fun initRecognizerIntent(): Single<Intent> {
        return useCases.initRecognizerIntent()
    }

    fun handleTalkOrStopClick() {
       clickSubject.onNext(talkBtnIcon)
    }

     fun changeTalkBtnIcon(){
       talkBtnIcon = if (talkBtnIcon==R.drawable.ic_mic_white_background_24){
           R.drawable.ic_pause_white_background_24
       }
       else{
           R.drawable.ic_mic_white_background_24
       }
       view.changeTalkIcon(talkBtnIcon)
    }

    fun checkIfTalkBtnIconChanged(){
        if (talkBtnIcon==R.drawable.ic_pause_white_background_24){
            clickSubject.onNext(talkBtnIcon)
        }
    }

    fun openSettingsActivity() {
        view.openSettingsActivity()
    }

    fun dispose(){
        compositeDisposable.clear()
    }

    private operator fun Disposable.unaryPlus(){
        compositeDisposable.add(this)
    }

    fun handleMenuClick(visibility: Int) {
        if (visibility==View.GONE)
             view.handleMenuClick(View.VISIBLE)
        else
              view.handleMenuClick(View.GONE)
    }
}