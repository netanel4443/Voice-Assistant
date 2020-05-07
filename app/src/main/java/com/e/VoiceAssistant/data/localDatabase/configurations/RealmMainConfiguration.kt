package com.e.VoiceAssistant.data.localDatabase.configurations

import com.e.VoiceAssistant.di.annotations.AppScope
import com.e.VoiceAssistant.data.localDatabase.modules.RealmMainModule
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

@AppScope
class RealmMainConfiguration @Inject constructor(
    private val module:RealmMainModule
){
    fun config(): RealmConfiguration {
      val config=  RealmConfiguration.Builder()
            .modules(module)
            .name("mainfile.realm")
            .deleteRealmIfMigrationNeeded()
            .build()
            Realm.setDefaultConfiguration(config)
        return config
         }
}