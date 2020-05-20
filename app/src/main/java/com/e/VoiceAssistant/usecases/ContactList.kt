package com.e.VoiceAssistant.usecases

import android.Manifest
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.provider.ContactsContract
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import io.reactivex.Observable

class ContactList {

    fun getContacts(context: Context): Observable<HashMap<String, String>> {

        return Observable.fromCallable {
            CheckOnlyPerrmission.check(context,Manifest.permission.READ_CONTACTS)
        }.flatMap {
            if (it){ //if permission granted , get contact list
                getCurrentLocale(context.resources,context)
             }
             else { //if not granted return an empty hashMap object
                Observable.just( HashMap())
             }
        }
    }

    fun getCurrentLocale(resources:Resources,context: Context):Observable<HashMap<String,String>> {
      return  Observable.fromCallable {
            val CountryID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resources.configuration.locales.get(0).country
            } else {
                resources.configuration.locale.country
            }

            var countryZipCode = ""

            val rl = resources.getStringArray(R.array.CountryCodes)
            for (i in rl.indices) {
                val g = rl[i].split(",").toTypedArray()
                if (g[1].trim { it <= ' ' } == CountryID.trim()) {
                    countryZipCode = g[0]
                //    println("CountryZipCode $countryZipCode")
                    break
                }
            }
          countryZipCode
        }.map {countryZipCode-> contactsList(context,countryZipCode) }
    }
    private fun contactsList(context: Context,countryCode:String):HashMap<String,String>{
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
                    number=number.replace("[^0-9]".toRegex(),"")
                    if (!number.startsWith(countryCode))
                         number=countryCode+number
                    hMap[name]=number
                    //   println("$name $number")
                }
            }catch (e:Exception){}
            finally { cursor.close()}
        }
        return hMap
    }

}