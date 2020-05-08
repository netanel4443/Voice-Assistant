package com.e.VoiceAssistant.ui.splashScreen

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import javax.inject.Inject

class LoadingSplashScreen : AppCompatActivity() {
   private lateinit var  reciever : LocalBroadcastManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reciever=LocalBroadcastManager.getInstance(this)
        reciever.registerReceiver(broadcastReceiver,
            IntentFilter("closeActivityWithBroadcast"))
    }

   private val broadcastReceiver=object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let{finishAndRemoveTask()}
        }
   }

   override fun onDestroy() {
       super.onDestroy()
       reciever.unregisterReceiver(broadcastReceiver)
   }
}