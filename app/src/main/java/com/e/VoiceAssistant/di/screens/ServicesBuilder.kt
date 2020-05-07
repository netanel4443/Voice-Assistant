package com.e.VoiceAssistant.di.screens

import com.e.VoiceAssistant.di.annotations.ServiceScope
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.SpeechRecognizerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServicesBuilder {

     @ServiceScope
     @ContributesAndroidInjector
     abstract fun contributeSpeechRecognizerService(): SpeechRecognizerService
}