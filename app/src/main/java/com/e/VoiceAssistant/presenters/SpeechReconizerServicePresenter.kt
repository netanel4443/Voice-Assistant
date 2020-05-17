package com.e.VoiceAssistant.presenters

import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.presenters.presentersStates.SpeechRecognizerServicePresenterState
import com.e.VoiceAssistant.usecases.PresenterUseCases
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
    private var talkBtnIcon=R.drawable.ic_mic_white_background_24
    private val clickSubject= PublishSubject.create<Int>()
    private var lastOperationIntent=Intent()
    private var operation=""

    init {
        +clickSubject
            .observeOn(AndroidSchedulers.mainThread())
            .throttle()
            .doOnNext { view.handleClick(it) }
            .subscribe({},{})
    }

    lateinit var view:SpeechRecognizerServicePresenterState

    fun bindView(view:SpeechRecognizerServicePresenterState){ this.view=view }

    fun initWindowManager(){
       val params=useCases.windowManagerAttributes()
           params.gravity = Gravity.TOP or Gravity.LEFT
       val darkScreenParams=useCases.windowManagerFullScreenAttributes()
           view.initWindowManager(params,darkScreenParams)
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

    fun checkIfSecondListenRequired(matches: ArrayList<String>,
                                appComponent: HashMap<String, AppsDetails>,
                                contactList: HashMap<String,String> ){
        if (operation=="הודעה"||operation=="message") {
            lastOperationIntent.putExtra(Intent.EXTRA_TEXT, matches[0])
            view.navigateToDesiredApp(lastOperationIntent)
        }
        else if ( operation=="וואטסאפ"){
            val uri =  Uri.parse(
                String.format(lastOperationIntent.data.toString()+"&text=%s" ,matches[0])
            )
            lastOperationIntent.data=uri
            view.navigateToDesiredApp(lastOperationIntent)
        }
        else {
            returnRequiredOperationIntent(matches,appComponent,contactList)
        }
        operation=""
    }

    fun returnRequiredOperationIntent(
        matches: ArrayList<String>,
        appComponent: HashMap<String, AppsDetails>,
        contactList: HashMap<String,String> ){

        +useCases.returnRequiredOperationIntent(matches,appComponent,contactList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({checkIntentType(it.first,it.second)},{})
    }

    private fun checkIntentType(requiredOpration: String, intent: Intent){
         if (requiredOpration=="הודעה"||requiredOpration=="text"||
             requiredOpration=="וואטסאפ" || requiredOpration=="whatsapp") {
             operation=requiredOpration
             lastOperationIntent=intent
             (view::secondListenToUser)()
         }
         else  {
             (view::navigateToDesiredApp)(intent)
         }
    }

    fun handleMenuClick(visibility: Int) {
        if (visibility==View.GONE)
             view.handleMenuClick(View.VISIBLE)
        else
             view.handleMenuClick(View.GONE)
    }

    private operator fun Disposable.unaryPlus(){
        compositeDisposable.add(this)
    }

    fun dispose(){
        compositeDisposable.clear()
    }
}