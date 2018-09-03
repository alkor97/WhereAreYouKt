package info.alkor.whereareyou.impl.contact.android

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import info.alkor.whereareyou.api.contact.ContactProvider
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

class ContactProviderImpl(private val context: Context) : ContactProvider {
    override fun findName(phone: PhoneNumber): Person {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone.value))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return Person(phone, cursor.getString(0))
            }
        }
        return Person(phone)
    }
}