package com.e.VoiceAssistant.usecases

import android.content.ContentResolver
import android.provider.ContactsContract
import com.e.VoiceAssistant.di.annotations.AppScope
import io.reactivex.Observable
import javax.inject.Inject

@AppScope
class ContactListUseCase @Inject constructor() {

    fun getContacts(cr: ContentResolver): Observable<HashMap<String, String>> {
        return Observable.fromCallable {
             contactsList(cr)
        }
    }

    private fun contactsList(cr: ContentResolver):HashMap<String,String>{
        val hMap = HashMap<String, String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

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
                    number=number.replace("[^0-9]".toRegex(),"")
                    hMap[name]=number
                    //   println("$name $number")
                }
            }catch (e:Exception){}
            finally { cursor.close()}
        }
        return hMap
    }

}