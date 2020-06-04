package com.e.VoiceAssistant.usecases

import android.content.ComponentName
import android.content.Intent
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.ComponentObject
import com.e.VoiceAssistant.di.annotations.ActivityScope
import io.reactivex.Observable
import javax.inject.Inject

@ActivityScope
class OpenDesiredAppPresenterUseCase @Inject constructor(){

    fun getDesiredIntent(appComponent:HashMap<String, AppsDetails>,
                         splitedResultsLhset:LinkedHashSet<String>  ): Observable<Intent> {

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
                    pckg=it
                    return@forEach
                }
            }

            val intent=navigateToDesiredApp(pckg,appComponent)
            return  Observable.just(intent)
    }

    private fun navigateToDesiredApp(pckg: String,appComponent:HashMap<String, AppsDetails>): Intent {
      //    println("pckggg $pckg")
            var intent = Intent()

            appComponent[pckg]?.run {
               val component=ComponentName(this.activity,this.pckg)
               intent= Intent(Intent.ACTION_MAIN)
               intent.addCategory(Intent.CATEGORY_LAUNCHER)
               intent.component =component
               intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
               intent
            }

        return intent
    }
}