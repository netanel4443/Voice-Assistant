package com.e.VoiceAssistant.ui.ads

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.*

class Adrequest {

    fun request():AdRequest{

        val adRequest=AdRequest.Builder()
        //    .addTestDevice("9FD989958EECA2C1FFF637324EA815FE")
            .build()
     //   val testDeviceIds = Arrays.asList("9FD989958EECA2C1FFF637324EA815FE")
     //   val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
     //   MobileAds.setRequestConfiguration(configuration)

        return adRequest
    }
}