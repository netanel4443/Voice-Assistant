package com.e.VoiceAssistant.ui.services.SpeechRecognizerService

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.e.VoiceAssistant.data.ComponentObject
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import com.e.VoiceAssistant.permissions.StartActivityToCheckPermission
import com.e.VoiceAssistant.presenters.SpeechReconizerServicePresenter
import com.e.VoiceAssistant.presenters.presentersStates.SpeechRecognizerServicePresenterState
import com.e.VoiceAssistant.sensors.CommandsForSpeechListenerService
import com.e.VoiceAssistant.sensors.HandleSpeechRecognition
import com.e.VoiceAssistant.ui.activities.MainActivity
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.onboarding.OnBoardingActivity
import com.e.VoiceAssistant.ui.services.BaseService
import com.e.VoiceAssistant.ui.splashScreen.LoadingSplashScreen
import com.e.VoiceAssistant.ui.uiHelpers.TouchHelper
import com.e.VoiceAssistant.ui.uiHelpers.touchListener.MultiTouchListener
import com.e.VoiceAssistant.usecases.ContactList
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
import com.jakewharton.rxbinding.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.floating_dark_screen.view.*
import kotlinx.android.synthetic.main.floating_widget_layout.view.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SpeechRecognizerService : BaseService(), SpeechRecognizerServicePresenterState {

    @Inject
    lateinit var presenter: SpeechReconizerServicePresenter
    @Inject
    lateinit var sharedPrefs: SharedPreferences

    private lateinit var talkIntent: SpeechRecognizer
    private lateinit var intnt: Intent
    private lateinit var floatingView: View
    private lateinit var floatingDarkScreen: View
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var darkScreenParams: WindowManager.LayoutParams
    private lateinit var parenOfDarkImage: RelativeLayout
    private lateinit var settings: Button
    private lateinit var closeIcon: Button
    private lateinit var help: Button
    private lateinit var floatingRepresentOperationsDialog: FloatingRepresentOperationsDialog
    private lateinit var darkImage : ImageView
    private lateinit var menu : Button
    private lateinit var talkBtn : Button
    private var appComponent = HashMap<String, ComponentObject>()
    private var concatList = HashMap<String, String>()
    private val TAG = "SpeechRecognizerService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val shouldStop = intent?.getBooleanExtra("stop", false)

        val state=intent?.getStringExtra("state").toString()

        when(state){
            "ADD"->{
            val activityName= intent?.getStringExtra("activityName").toString()
            val pckg= intent?.getStringExtra("package").toString()
            val name= intent?.getStringExtra("name")

                name?.let {
                    val component = ComponentObject(
                        activityName,
                        pckg
                    )
                        appComponent[name]=component
                }
            }

            "REMOVE"->{
                val name= intent?.getStringExtra("name")
                    name?.let { appComponent.remove(name) }
            }
        }

        shouldStop?.let {
            if (shouldStop) { stopSelf() }
        }
        return Service.START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        firstInits()
        initViews()
        initAppAndPackageList()

        +RxView.clicks(settings).throttle().subscribe {
            presenter.openSettingsActivity()
        }

        +RxView.clicks(menu).throttle().subscribe {
            presenter.handleMenuClick(settings.visibility)
        }

        +RxView.clicks(help).throttle().subscribe {
           floatingRepresentOperationsDialog.show()
        }

        +RxView.clicks(darkImage).subscribe {
            presenter.handleMenuClick(settings.visibility)
        }

        closeIcon.setOnClickListener { stopSelf() }

        talkBtn.setOnTouchListener(MultiTouchListener{ handleTalkBtnTouchEvents(it) })
    }

    private fun handleTalkBtnTouchEvents(touchHelper: TouchHelper) {
        when (touchHelper) {
            is TouchHelper.moveEvent -> {
                params.x = touchHelper.xPos
                params.y = touchHelper.yPos
                windowManager.updateViewLayout(floatingView, params)
            }
            is TouchHelper.TalkOrStopClickEvent -> {
                presenter.handleTalkOrStopClick()
            }
        }
    }

    private fun firstInits() {
        presenter.bindView(this)
        floatingRepresentOperationsDialog = FloatingRepresentOperationsDialog(this)
        shouldShowOnBoarding()
        //   ForegroundNotification().notification(this)
    }

    private fun shouldShowOnBoarding() {
        val prefs = sharedPrefs.getBoolean("seen", false)
        if (!prefs) {
            val intent = Intent(this, OnBoardingActivity::class.java)
            navigateToDesiredApp(intent)
        } else
            startLoginSplashScreen()
    }

    private fun initViews() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null)
        floatingDarkScreen = LayoutInflater.from(this).inflate(R.layout.floating_dark_screen, null)
        closeIcon = floatingView.closeServiceBtn
        settings = floatingView.floatingSettingsButton
        help = floatingView.floatingHelp
        parenOfDarkImage = floatingDarkScreen.trashMainParent
        darkImage = floatingDarkScreen.floatingDarkImage
        menu = floatingView.floatingMenu
        talkBtn = floatingView.floatingTalkImg
    }

    private fun startLoginSplashScreen() {
        val intent = Intent(this, LoadingSplashScreen::class.java)
        navigateToDesiredApp(intent)
    }

    private fun closeLoginSplashScreen() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("closeActivityWithBroadcast"))
    }

    private fun initAppAndPackageList() {

        val first = ContactList().getContacts(this)
            .doOnNext { setContactList(it) }
        val second = AppAndPackageList().init(this)
            .doOnNext { setAppComponentList(it) }
        val third = presenter.initRecognizerIntent().toObservable()
            .doOnNext { setIntent(it) }

        val observableList = listOf(first, second, third)

        +Observable.combineLatest(observableList) {}
            .subscribeOnIoAndObserveOnMain()
            .doOnComplete {//should happen on mainThread after inits are completed
                presenter.initWindowManager()
                initSpeechRecognizer()
                closeLoginSplashScreen()
                floatingRepresentOperationsDialog.show()
            }
            .subscribe({}, {})
    }

    private fun setContactList(list: HashMap<String, String>) {
        concatList = list
    }

    private fun setAppComponentList(list: HashMap<String, ComponentObject>) {
        appComponent = list
        presenter.getAppsList(appComponent)
    }

    private fun setIntent(intent: Intent) {
        intnt = intent
    }

    override fun initWindowManager(params: WindowManager.LayoutParams,
                                   trashParams: WindowManager.LayoutParams) {
        this.params = params
        darkScreenParams = trashParams
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingDarkScreen, trashParams)
        windowManager.addView(floatingView, params)
    }

    private fun initSpeechRecognizer() {
        talkIntent = SpeechRecognizer.createSpeechRecognizer(this)
        talkIntent.setRecognitionListener(HandleSpeechRecognition(object :CommandsForSpeechListenerService{
            override fun onError(message: String) {
                toast(message)
            }
            override fun changeTalkIcon() {
                presenter.checkIfTalkBtnIconChanged()
            }
            override fun checkForResults(matches:ArrayList<String>) {
                presenter.returnRequiredOperationIntent(matches,appComponent,concatList)
            }
        })
        )
    }

    override fun handleClick(icon: Int) {
        if (StartActivityToCheckPermission.check(this, permission.RECORD_AUDIO)) {
            if (R.drawable.ic_mic_white_background_24 == icon) {
                talkIntent.startListening(intnt)
                presenter.changeTalkBtnIcon()
            } else {
                talkIntent.stopListening()
                presenter.changeTalkBtnIcon()
            }
        }
    }

    override fun changeTalkIcon(icon: Int) {
        floatingView.floatingTalkImg.setBackgroundResource(icon)
    }

    override fun navigateToDesiredApp(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                       Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

        if (checkIfPermissionRequired(intent))
        {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        }
        else {
            toastLong(R.string.permission_required)
        }
    }

    private fun checkIfPermissionRequired(intent: Intent):Boolean {
       return when(intent.action){
            Intent.ACTION_DIAL->CheckOnlyPerrmission.check(this,permission.READ_CONTACTS)

            else ->true
        }
    }

    override fun openSettingsActivity() {
        val intent = Intent(this, MainActivity::class.java)
        navigateToDesiredApp(intent)
    }

    override fun addAppsFromMemory(apps: HashMap<String, Pair<String, String>>) {
        apps.keys.forEach {
            val name = ComponentObject(
                apps[it]!!.first,
                apps[it]!!.second
            )
            appComponent[it] = name
        }
    }

    override fun handleMenuClick(visibility: Int) {
        settings.visibility = visibility
        closeIcon.visibility = visibility
        help.visibility = visibility
        parenOfDarkImage.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
//        val nm=  this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        nm.cancel(1)
        presenter.dispose()
        windowManager.removeView(floatingView)
        windowManager.removeView(floatingDarkScreen)
        talkIntent.destroy()
    }
}