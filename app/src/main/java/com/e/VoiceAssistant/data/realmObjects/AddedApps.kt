package com.e.VoiceAssistant.data.realmObjects

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AddedApps : RealmObject() {

    @PrimaryKey
    var newName:String=""
    var realName:String=""
    var activityName:String=""
    var pckg:String=""
}