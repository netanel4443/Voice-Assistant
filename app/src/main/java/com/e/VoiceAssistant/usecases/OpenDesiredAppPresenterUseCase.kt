package com.e.VoiceAssistant.usecases

import android.content.ComponentName
import android.content.Intent
import com.e.VoiceAssistant.data.ComponentObject

class OpenDesiredAppPresenterUseCase {

    fun getDesiredIntent(appComponent:HashMap<String, ComponentObject>,
                         splitedResultsLhset:LinkedHashSet<String>  ): Intent {

            val resultss = HashSet<String>()
            var pckg=""

            appComponent.keys.forEach { key ->

                var tmpResults=""
                var tmpResult =""
                val iterator = splitedResultsLhset.iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()

                    if ((key.startsWith(next) && next.isNotBlank() && tmpResults.isEmpty())
                        || ((key.startsWith(tmpResults) && tmpResults.isNotBlank()))) {

                        tmpResults += next

                        if (key.startsWith(tmpResults)) {
                            tmpResult = tmpResults
                            tmpResults += " "
                        }
                        else
                            tmpResults=tmpResult+" "
                    }
                }
                if (tmpResult.isNotBlank()) resultss.add(tmpResult)
            }

            resultss.forEach {
                //    println("results $it")
                if (appComponent.containsKey(it)) {
//                    action(SpeechStates.Success,it)
                    pckg=it
                    return@forEach
                }
            }
        return  navigateToDesiredApp(pckg,appComponent)
        }

    private fun navigateToDesiredApp(pckg: String,appComponent:HashMap<String, ComponentObject>): Intent {
      //    println("pckggg $pckg")
            var intent = Intent()

           // intent.component = appComponent[pckg]

           appComponent[pckg]?.run {
               val component=ComponentName(this.activityName,this.pckg)
               intent= Intent(Intent.ACTION_MAIN)
               intent.addCategory(Intent.CATEGORY_LAUNCHER)
               intent.component =component
               intent
             }

        return intent
    }
}