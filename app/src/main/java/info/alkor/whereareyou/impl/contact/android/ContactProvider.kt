package info.alkor.whereareyou.impl.contact.android

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

class ContactProvider(private val context: Context) {
    fun findName(phone: PhoneNumber): Person {
        if (!canReadContacts())
            return Person(phone)

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone.value))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return Person(phone, it.getString(0))
            }
        }
        return Person(phone)
    }

    private fun canReadContacts() = (context as AppContext).permissionAccessor.isPermissionGranted(Manifest.permission.READ_CONTACTS)
}