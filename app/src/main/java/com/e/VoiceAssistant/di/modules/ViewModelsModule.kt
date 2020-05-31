package com.e.VoiceAssistant.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.di.annotations.ViewModelKey
import com.e.VoiceAssistant.di.viewmodels.ViewModelProviderFactory
import com.e.VoiceAssistant.viewmodels.AddCustomAppNameViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddCustomAppNameViewModel::class)
    abstract fun bindSpeechRecognitionViewModel(addCustomAppNameViewModel: AddCustomAppNameViewModel):ViewModel

}
