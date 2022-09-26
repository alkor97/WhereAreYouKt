package info.alkor.whereareyou.impl.communication.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import info.alkor.whereareyou.impl.contact.android.ContactProvider
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.PhoneNumber
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SmsReceiver : BroadcastReceiver() {
    @ExperimentalCoroutinesApi
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val contacts = ContactProvider(context.applicationContext)
            Telephony.Sms.Intents.getMessagesFromIntent(intent).forEach {
                val phoneNumber = PhoneNumber(it.originatingAddress!!)
                if (phoneNumber.isValid()) {
                    val person = contacts.findName(phoneNumber)
                    (context.applicationContext as AppContext).handleMessage(person, it.messageBody)
                }
            }
        }
    }
}