package com.e.VoiceAssistant.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import com.e.VoiceAssistant.permissions.StartActivityToCheckPermission
import com.e.VoiceAssistant.presenters.TalkAndResultsPresenter
import com.e.VoiceAssistant.presenters.presentersStates.TalkAndResultsPresenterView
import com.e.VoiceAssistant.sensors.CommandsForSpeechListenerService
import com.e.VoiceAssistant.sensors.HandleSpeechRecognition
import com.e.VoiceAssistant.ui.ads.Adrequest
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.OperationsKeyWordsAdapter
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.PossibleResultsRecyclerViewAdapter
import com.e.VoiceAssistant.usecases.ContactList
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
import com.jakewharton.rxbinding.view.RxView
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_talk_and_results.*
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap


class TalkAndResultsActivity : DaggerAppCompatActivity() , TalkAndResultsPresenterView {

    @Inject lateinit var presenter: TalkAndResultsPresenter
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private lateinit var talkIntent: SpeechRecognizer
    private lateinit var intnt: Intent
    private var appsDetailsHmap=HashMap<String, AppsDetails>()
    private var concatList = HashMap<String, String>()
    private val TAG = "TalkAndResultsActivity"
    private val compositeSubscription= CompositeSubscription()
    private val compositeDisposable= CompositeDisposable()
    private  lateinit var adapter: PossibleResultsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_and_results)

        firstInits()
        initContactListAndSpeechRecognition ()

        clickToTalkOrStopBtn.setOnClickListener {
            presenter.handleTalkOrStopClick()
        }

        +RxView.clicks(settings).throttle().subscribe {
            val intent = Intent(this, AddCustomAppNameActivity::class.java)
            startActivity(intent)
            //  navigateToDesiredApp(intent)
        }

        +RxView.clicks(floatingHelp).throttle().subscribe {
            FloatingRepresentOperationsDialog(this).show()
        }
    }


    private fun firstInits() {
        presenter.bindView(this)
        appsDetailsHmap=appsDetailsSingleton.appsAndStoredAppsDetails
        initRecyclerView()
        initOperationsRecyclerView()
        talk_and_result_activity_adView.loadAd(Adrequest().request())
    }

    private fun initOperationsRecyclerView() {
    val operationAdapter= OperationsKeyWordsAdapter()
        operationsWordsRecyclerView.adapter=operationAdapter
        operationsWordsRecyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        operationsWordsRecyclerView.setHasFixedSize(true)
        operationAdapter.attachData(resources.getStringArray(R.array.operations_keywords).toHashSet())
    }

    private fun initRecyclerView() {
        adapter= PossibleResultsRecyclerViewAdapter()
        adapter.itemClick={presenter.changeSelectedResult(it,appsDetailsHmap)}
        possibleResultsRecyclerView.adapter=adapter
        possibleResultsRecyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        possibleResultsRecyclerView.setHasFixedSize(true)
    }

    private fun initContactListAndSpeechRecognition() {

        val first = ContactList().getContacts(this)
            .doOnNext { setContactList(it) }
        val second = presenter.initRecognizerIntent().toObservable()
            .doOnNext { setIntnt(it) }

        val observableList = listOf(first, second)

        +Observable.combineLatest(observableList) {}
            .subscribeOnIoAndObserveOnMain()
            .doOnComplete {
                /*according to docs this should be initialized on mainThread after inits are completed*/
                initSpeechRecognizer()
            }
            .subscribe({}, {})
    }


    private fun setContactList(list: HashMap<String, String>) {
        concatList = list
    }

    private fun setIntnt(intent: Intent) {
        intnt = intent
    }

    private fun initSpeechRecognizer() {
        talkIntent = SpeechRecognizer.createSpeechRecognizer(this)
        talkIntent.setRecognitionListener(
            HandleSpeechRecognition(object : CommandsForSpeechListenerService {
            override fun onError(message: String) {
                toast(message)
            }
            override fun changeTalkIcon() {
                presenter.checkIfTalkBtnIconChanged()
            }
            override fun checkForResults(matches:ArrayList<String>) {
                presenter.checkIfSecondListenRequired(matches,appsDetailsHmap,concatList)
            }

            override fun relisten() {
            }
            })
        )
    }

    override fun handleClick(icon: Int) {
        if (StartActivityToCheckPermission.check(this, Manifest.permission.RECORD_AUDIO)) {
            if (R.drawable.ic_mic_white_background_24 == icon) {
                talkIntent.startListening(intnt)
                presenter.changeTalkBtnIcon()
            } else {
                talkIntent.stopListening()
                /*timer added to cancel because the api sometimes stuck at "Busy state" and we can't
                *perform a new operation*/
                +Observable.timer(500,TimeUnit.MILLISECONDS)
                    .subscribeOnIoAndObserveOnMain()
                    .subscribe{talkIntent.cancel() }
                presenter.changeTalkBtnIcon()
            }
        }
    }

    override fun changeTalkIcon(icon: Int) {
        clickToTalkOrStopBtn.setBackgroundResource(icon)
    }

    override fun navigateToDesiredApp(intent: Intent, results: HashSet<ResultsData>,dataType: Int) {
        println("intent ${intent.data}")
        if (checkIfPermissionRequired(intent))
        {

            try {
                startActivity(intent)
                adapter.attachData(results,dataType,intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        }
        else {
            toastLong(R.string.permission_required)
        }
    }

    override fun navigateToDesiredApp(intent: Intent) {
        if (checkIfPermissionRequired(intent)) {
            try {
                startActivity(intent)
            }
            catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        }
        else {
            toastLong(R.string.permission_required)
        }
    }

    override fun showResults(results: HashSet<ResultsData>, dataType: Int) {
        adapter.attachData(results,dataType)
    }

    override fun secondListenToUser(contacts:HashSet<ResultsData>, dataType: Int, intent: Intent) {
        /** timer is needed because of throttle click(rxBinding) .
           After a user clicks on the [talk] button when he wants to pause the speech recognition*/
        adapter.attachData(contacts,0,intent)

        +presenter.countDownTimerAnimation()
            .subscribe({
                counterTalkAndResultParent.visibility= View.GONE
                presenter.handleTalkOrStopClick()
            }, {})
    }

    override  fun timerAnimation(counter:Int){
        counterTalkAndResultParent.visibility= View.VISIBLE
        val fadeOut=   AnimationUtils.loadAnimation(this, R.anim.fade_out)
        counterTalkAndResult.startAnimation(fadeOut)
        counterTalkAndResult.text=getString(R.string.record_in) +" " + "$counter"
    }

    private fun checkIfPermissionRequired(intent: Intent):Boolean {
        return when(intent.action){
            Intent.ACTION_DIAL-> CheckOnlyPerrmission.check(this, Manifest.permission.READ_CONTACTS)
            else ->true
        }
    }

    override fun openSettingsActivity() {
        val intent = Intent(this, AddCustomAppNameActivity::class.java)
        startActivity(intent)
    }
    override fun onResume() {
        super.onResume()
        talk_and_result_activity_adView.resume()
    }

    override fun onPause() {
        super.onPause()
        talk_and_result_activity_adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        talk_and_result_activity_adView?.apply{
            removeAllViews()
            destroy()
        }
        presenter.dispose()
        talkIntent.destroy()
        compositeDisposable.clear()
        compositeSubscription.clear()
    }

    private inline operator fun<reified T : Subscription> T.unaryPlus() =
        compositeSubscription.add(this)

    private inline operator fun<reified T : Disposable> T.unaryPlus() =
        compositeDisposable.add(this)
}


