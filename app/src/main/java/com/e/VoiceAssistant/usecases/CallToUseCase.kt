package com.e.VoiceAssistant.usecases

import android.content.Intent

class CallToUseCase {

    fun call(splitedResultsLhset:LinkedHashSet<String>,
             keys:MutableSet<String>) {

        val resultss = HashSet<String>()
        var pckg=""

        keys.forEach { key ->

            var tmpResults=""
            var tmpResult =""
            val iterator = splitedResultsLhset.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()

                if ((key.startsWith(next) && next.isNotBlank() && key.length>1)) {
                    resultss.add(key)
                }
            }

        }

        resultss.forEach {
                println("results $it")
            if (keys.contains(it)) {
//                    action(SpeechStates.Success,it)
                pckg=it
                    //  return@forEach
            }
        }
    }
}