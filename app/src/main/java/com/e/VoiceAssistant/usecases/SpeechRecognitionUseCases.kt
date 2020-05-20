package com.e.VoiceAssistant.usecases

import android.content.Intent
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@ActivityScope
class SpeechRecognitionUseCases @Inject constructor(private val repo: RealmRepo) {

    fun initRecognizerIntent(): Single<Intent> {
        return RecognizerIntentInit().init()
    }

    fun saveApp(newName: String, pckg: String,
        realName: String, activityName: String): Completable {
        return repo.saveOrUpdateAppList(newName, pckg, realName, activityName)
    }

    fun deleteApp(name: String): Completable {
        return repo.deleteApp(name)
    }

}

