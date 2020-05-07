package com.e.VoiceAssistant.di.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class ViewModelProviderFactory @Inject constructor(private val creators: Map<Class<out ViewModel>,@JvmSuppressWildcards Provider<ViewModel>>):
    ViewModelProvider.Factory{

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        var creator = creators[modelClass]
        if (creator == null) {

            for (entry in creators.entries) {

                if (modelClass.isAssignableFrom(entry.key)) {
                    creator = entry.value
                    break
                }
            }
        }

        requireNotNull(creator) { "unknown model class $modelClass" }

        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}