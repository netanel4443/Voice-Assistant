package com.e.VoiceAssistant.ui.activities

import android.widget.FrameLayout
import com.e.VoiceAssistant.ui.ads.Adrequest
import com.e.VoiceAssistant.ui.dialogs.CircleProgressBarDialog
import com.e.VoiceAssistant.utils.printIfDebug
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseAdsActivity:DaggerAppCompatActivity() {

    protected val compositeDisposable=CompositeDisposable()
    private var ad:AdView?=null
    //    @Inject lateinit var provider:ViewModelProvider.Factory
//
//    protected inline fun <reified T : ViewModel> getViewModel(): T =
//       ViewModelProvider(this , provider)[T::class.java]

    protected fun loadAd(adLayout: FrameLayout,adUnitId:String){
      val ad=AdView(this)
        ad.adSize=AdSize.BANNER
        ad.adUnitId=adUnitId
        adLayout.addView(ad)
        ad.loadAd(Adrequest().request())
    }
    override fun onResume() {
        super.onResume()
        ad?.resume()
    }

    override fun onPause() {
        super.onPause()
        ad?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        ad?.apply{
            removeAllViews()
            destroy()
        }
        compositeDisposable.clear()
    }

    protected inline operator fun <reified T : Disposable> T.unaryPlus() =
        compositeDisposable.add(this)
    
    val fakeUnitId: String  = "ca-app-pub-3940256099942544/6300978111"
    val talkAndResultActivityUnitId  ="ca-app-pub-8194360858461012/4556516167"
    val addCustomAppNameActivityUnitId = "ca-app-pub-8194360858461012/9582766118"

}
