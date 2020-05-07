package com.e.VoiceAssistant.di.screens

import com.e.VoiceAssistant.di.annotations.FragmentScope
import com.e.VoiceAssistant.ui.fragments.AddedAppsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilders {

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun bindAddedAppsFragment():AddedAppsFragment
}