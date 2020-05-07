package com.e.VoiceAssistant.data.repo

import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.localDatabase.configurations.RealmMainConfiguration
import com.e.VoiceAssistant.data.realmObjects.AddedApps
import com.e.VoiceAssistant.di.annotations.AppScope
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@AppScope
class RealmRepo @Inject constructor(
    private val config:RealmMainConfiguration
){

    fun saveOrUpdateAppList(newName:String,pckg:String,realName:String,activityName:String):Completable{

      return  Completable.create {emitter->
        val realm = Realm.getInstance(config.config())
        try{
              realm.executeTransaction {
                  val obj=realm.createObject(AddedApps::class.java,newName)
                  obj.realName=realName
                  obj.pckg=pckg
                  obj.activityName=activityName
              //    println("${newName}  ${realName} ${activityName}  ${pckg}")
                  realm.insertOrUpdate(obj)
              }
            emitter.onComplete()
          }
          catch (e:Exception){ emitter.onError(e) }
          finally { realm.close()}
      }
    }

   fun deleteApp(name:String):Completable{
       return Completable.create{emitter->
           val realm = Realm.getInstance(config.config())
           try {
               realm.executeTransaction {

                   val obj = it.where(AddedApps::class.java)
                       .equalTo("newName", name)
                       .findFirst()
                   obj?.deleteFromRealm()
                   emitter.onComplete()
               }
           }catch (e:Exception){//e.printStackTrace();
               emitter.onError(e)}
           finally { realm.close();}
       }
   }

    fun getAppsList(): Single<HashMap<String, SavedAppsDetails>> {
        return Single.fromCallable {
            val realm=Realm.getInstance(config.config())
            val hMap=HashMap<String, SavedAppsDetails>()

             realm.use {
                 try {
                     val obj=realm.where(AddedApps::class.java)
                         .findAll()
                     obj?.forEach {
                         hMap[it.newName]=
                             SavedAppsDetails(
                                 it.realName,
                                 it.pckg,
                                 it.activityName,
                                 it.newName
                             )
                    //     println("${it.newName} ${it.realName} ${it.activityName}  ${it.pckg} get")
                     }
                }
                 catch (e:Exception){//e.printStackTrace()
                 }
             }
          hMap
       }
    }

    fun deleteAppsFromDB(list: ArrayList<String>): Completable {
        return Completable.fromAction{
            val realm=Realm.getInstance(config.config())
            realm.use {
            try {
             //   println("deleted")

                list.forEach {name->
                    realm.where(AddedApps::class.java)
                        .equalTo("newName",name)
                        .findFirst()
                        ?.deleteFromRealm()
                 //   println("deleted")
                }
            }catch (e:Exception){//e.printStackTrace()
            }
        }
        }
    }
}
