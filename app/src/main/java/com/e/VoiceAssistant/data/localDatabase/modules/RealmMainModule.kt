package com.e.VoiceAssistant.data.localDatabase.modules

import com.e.VoiceAssistant.data.realmObjects.AddedApps
import com.e.VoiceAssistant.di.annotations.AppScope
import io.realm.annotations.RealmModule
import javax.inject.Inject

@AppScope
@RealmModule(classes = [AddedApps::class])
class RealmMainModule @Inject constructor()