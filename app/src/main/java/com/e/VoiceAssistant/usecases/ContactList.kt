package com.e.VoiceAssistant.usecases

import android.Manifest
import android.content.Context
import android.provider.ContactsContract
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import com.e.VoiceAssistant.utils.printMessage
import io.reactivex.Observable
import java.util.*
import kotlin.collections.HashMap

class ContactList {

    fun getContacts(context: Context): Observable<HashMap<String, String>> {

        return Observable.fromCallable {
            CheckOnlyPerrmission.check(context,Manifest.permission.READ_CONTACTS)
        }.map {
             if (it){
                 contactsList(context)//if permission granted , get contact list
             }
            else
             {//if not granted return an empty hashMap object
                 HashMap<String,String>()
             }
        }
    }
    private fun contactsList(context: Context):HashMap<String,String>{
        val hMap = HashMap<String, String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cr = context.contentResolver
        val cursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )
        cursor?.let {
            try {
                val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                var name:String
                var number :String

                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex).toLowerCase()
                    number = cursor.getString(numberIndex)
                    name=name.replace("[^a-zא-ת ]".toRegex(),"")
                    number=number.replace("[^0-9+]".toRegex(),"")
                    hMap[name]=number
                 //   println("$name $number")
                }
            }catch (e:Exception){}
            finally { cursor.close()}
        }
        return hMap
    }

}